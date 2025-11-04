package com.dyson.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.concurrent.Semaphore;

@Service
public class BaiduMapService {

    private static final Logger logger = LoggerFactory.getLogger(BaiduMapService.class);

    @Value("${baidu.map.api.key:T9FXDrUaHlK0hd4A0TCg9EZMGLD3X6xt}")
    private String apiKey;

    // 并发控制：最多同时3个请求（降低并发以避免触发配额限制）
    private static final int MAX_CONCURRENT_REQUESTS = 3;
    private final Semaphore requestSemaphore = new Semaphore(MAX_CONCURRENT_REQUESTS, true);
    
    // 请求间隔：每个请求之间至少间隔500ms，避免触发频率限制
    private static final long REQUEST_INTERVAL_MS = 500;
    private volatile long lastRequestTime = 0;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public BaiduMapService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取从起点到终点的实际路径距离（公里）
     * 
     * @param originLat 起点纬度
     * @param originLng 起点经度
     * @param destLat 终点纬度
     * @param destLng 终点经度
     * @return 实际路径距离（公里），如果API调用失败返回null
     */
    public Double getRouteDistance(BigDecimal originLat, BigDecimal originLng, 
                                   BigDecimal destLat, BigDecimal destLng) {
        return getRouteDistanceWithRetry(originLat, originLng, destLat, destLng, 3);
    }

    /**
     * 带重试机制的路径距离获取
     */
    private Double getRouteDistanceWithRetry(BigDecimal originLat, BigDecimal originLng, 
                                            BigDecimal destLat, BigDecimal destLng, int maxRetries) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            boolean isConcurrentLimit = false;
            try {
                // 控制并发：获取信号量许可
                requestSemaphore.acquire();
                try {
                    // 控制请求频率：确保请求间隔
                    long currentTime = System.currentTimeMillis();
                    long timeSinceLastRequest = currentTime - lastRequestTime;
                    if (timeSinceLastRequest < REQUEST_INTERVAL_MS) {
                        Thread.sleep(REQUEST_INTERVAL_MS - timeSinceLastRequest);
                    }
                    lastRequestTime = System.currentTimeMillis();

                    Double result = callRouteDistanceAPI(originLat, originLng, destLat, destLng);
                    
                    // 如果成功，直接返回
                    if (result != null) {
                        return result;
                    }
                    
                } catch (ConcurrentLimitException e) {
                    // 捕获并发限制异常，标记需要重试并等待更长时间
                    isConcurrentLimit = true;
                    logger.warn("检测到并发限制错误，将在重试前等待更长时间: {}", e.getMessage());
                } finally {
                    requestSemaphore.release();
                }
                
                // 重试前等待：指数退避策略
                // 如果是并发限制错误，等待时间更长
                if (attempt < maxRetries - 1) {
                    long waitTime;
                    if (isConcurrentLimit) {
                        // 并发限制：等待更长时间 (3s, 6s, 12s...)
                        waitTime = (long) Math.pow(2, attempt) * 3000;
                    } else {
                        // 普通错误：标准等待时间 (1s, 2s, 4s...)
                        waitTime = (long) Math.pow(2, attempt) * 1000;
                    }
                    logger.debug("API调用失败，{}ms后重试 (尝试 {}/{})", waitTime, attempt + 2, maxRetries);
                    Thread.sleep(waitTime);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("等待过程中被中断", e);
                return null;
            } catch (ConcurrentLimitException e) {
                // 处理从finally块外部抛出的异常
                isConcurrentLimit = true;
                if (attempt < maxRetries - 1) {
                    long waitTime = (long) Math.pow(2, attempt) * 3000;
                    logger.debug("并发限制错误，{}ms后重试 (尝试 {}/{})", waitTime, attempt + 2, maxRetries);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            } catch (Exception e) {
                logger.error("调用百度地图路径规划API时发生异常 (尝试 {}/{})", attempt + 1, maxRetries, e);
                if (attempt == maxRetries - 1) {
                    return null;
                }
                // 其他异常也进行重试，使用标准等待时间
                if (attempt < maxRetries - 1) {
                    long waitTime = (long) Math.pow(2, attempt) * 1000;
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 实际调用百度地图API的方法
     */
    private Double callRouteDistanceAPI(BigDecimal originLat, BigDecimal originLng, 
                                       BigDecimal destLat, BigDecimal destLng) {
        try {
            // 构建API请求URL
            // 百度地图路径规划API：驾车路线规划
            // 注意：百度地图API的坐标格式是"纬度,经度"（lat,lng），使用BD09坐标系
            // 使用HTTPS协议，更安全且符合百度地图API规范
            String origin = originLat + "," + originLng;
            String destination = destLat + "," + destLng;
            
            // 坐标是数字，不需要URL编码
            String url = String.format(
                "https://api.map.baidu.com/direction/v2/driving?origin=%s&destination=%s&ak=%s",
                origin, destination, apiKey
            );
            
            logger.debug("调用百度地图路径规划API: origin={}, destination={}", origin, destination);

            // 发送HTTP请求
            String response = restTemplate.getForObject(url, String.class);

            // 解析JSON响应
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // 检查状态码
            int status = jsonNode.path("status").asInt();
            if (status != 0) {
                // API调用失败，返回null，让调用方使用备用方案
                String errorMsg = jsonNode.path("message").asText();
                
                // 状态码240表示"APP服务被禁用"，需要用户在百度地图开放平台启用路径规划服务
                if (status == 240) {
                    logger.warn("百度地图API调用失败 - 状态码240（APP服务被禁用）。" +
                              "请在百度地图开放平台（https://lbsyun.baidu.com/）检查：" +
                              "1. 应用列表中选择对应应用；" +
                              "2. 在'服务管理'中启用'路线规划'服务；" +
                              "3. 确认API Key（AK）是否正确。");
                } 
                // 状态码401表示并发超限，需要重试
                else if (status == 401) {
                    logger.warn("百度地图API调用失败 - 状态码401（并发超限），将自动重试: {}", errorMsg);
                    // 抛出特殊异常以便上层识别并重试
                    throw new ConcurrentLimitException(errorMsg);
                } 
                else {
                    logger.warn("百度地图路径规划API调用失败，状态码: {}, 消息: {}", status, errorMsg);
                }
                return null;
            }

            // 提取距离（单位：米）
            JsonNode result = jsonNode.path("result");
            JsonNode routes = result.path("routes");
            if (routes.isArray() && routes.size() > 0) {
                JsonNode firstRoute = routes.get(0);
                int distanceMeters = firstRoute.path("distance").asInt();
                
                // 转换为公里
                return distanceMeters / 1000.0;
            }

            return null;
        } catch (ConcurrentLimitException e) {
            // 重新抛出并发限制异常，让重试机制处理
            throw e;
        } catch (Exception e) {
            // 异常处理：网络错误、解析错误等
            logger.error("调用百度地图路径规划API时发生异常", e);
            return null;
        }
    }

    /**
     * 并发限制异常，用于触发重试
     */
    private static class ConcurrentLimitException extends RuntimeException {
        public ConcurrentLimitException(String message) {
            super(message);
        }
    }

    /**
     * 批量获取路径距离（已包含限流和重试机制）
     * 注意：为了遵守百度地图API的频率限制，请求会串行执行，并在每个请求之间添加延迟
     */
    public void fillRouteDistances(java.util.List<com.dyson.demo.dto.StationDTO> stations, 
                                   BigDecimal originLat, BigDecimal originLng) {
        // 串行处理，通过Semaphore和请求间隔控制并发和频率
        for (com.dyson.demo.dto.StationDTO station : stations) {
            if (station.getDistance() == null) {
                Double routeDistance = getRouteDistance(
                    originLat, originLng,
                    station.getLat(), station.getLng()
                );
                // 如果API调用成功，更新距离；否则保持为null，后续使用备用方案
                if (routeDistance != null) {
                    station.setDistance(routeDistance);
                }
            }
        }
    }
}

