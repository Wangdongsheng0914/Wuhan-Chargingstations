-- 为stations表创建索引以优化地理位置查询性能
-- 这些索引将显著提升根据经纬度范围查询充电站的速度

-- 单独索引（如果联合索引不可行时的备选方案）
CREATE INDEX IF NOT EXISTS idx_stations_lat ON stations(lat);
CREATE INDEX IF NOT EXISTS idx_stations_lng ON stations(lng);

-- 联合索引（推荐，对lat和lng的联合查询更高效）
CREATE INDEX IF NOT EXISTS idx_stations_location ON stations(lat, lng);

-- 显示创建结果
SELECT 
    indexname, 
    indexdef 
FROM pg_indexes 
WHERE tablename = 'stations' 
    AND indexname LIKE 'idx_stations%';

