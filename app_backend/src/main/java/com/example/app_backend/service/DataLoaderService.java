package com.example.app_backend.service;

import com.example.app_backend.entity.BikeLane;
import com.example.app_backend.entity.SportSpot;
import com.example.app_backend.repository.BikeLaneRepository;
import com.example.app_backend.repository.SportSpotRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonArray;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.DecoderContext;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataLoaderService {

    private final SportSpotRepository sportSpotRepository;
    private final BikeLaneRepository bikeLaneRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // Главный метод, запускающий независимые проверки
    @PostConstruct
    public void init() {
        log.info("Начинаем проверку и загрузку данных...");
        loadSportSpots();
        loadBikeLanes();
    }

    private void loadSportSpots() {
        if (sportSpotRepository.count() > 0) {
            log.info("Таблица sport_spots уже заполнена. Пропускаем загрузку.");
            return;
        }
        try {
            InputStream inputStream = getClass().getResourceAsStream("/workouts.bson");
            if (inputStream == null) {
                log.warn("Файл workouts.bson не найден в resources!");
                return;
            }
            byte[] bytes = inputStream.readAllBytes();
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            BsonBinaryReader reader = new BsonBinaryReader(buffer);
            BsonDocumentCodec codec = new BsonDocumentCodec();

            int count = 0;
            while (buffer.hasRemaining() && reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                BsonDocument doc = codec.decode(reader, DecoderContext.builder().build());
                SportSpot spot = new SportSpot();

                if (doc.containsKey("global_id")) {
                    spot.setGlobalId(doc.getNumber("global_id").longValue());
                } else {
                    spot.setGlobalId((long) count);
                }
                spot.setDatasetId(898L);

                BsonDocument dataNode = doc.containsKey("Cells") ? doc.getDocument("Cells") : doc;

                if (dataNode.containsKey("ObjectName") && !dataNode.isNull("ObjectName")) {
                    spot.setObjectName(dataNode.getString("ObjectName").getValue());
                }
                if (dataNode.containsKey("Address") && !dataNode.isNull("Address")) {
                    spot.setAddress(dataNode.getString("Address").getValue());
                }
                if (dataNode.containsKey("District") && !dataNode.isNull("District")) {
                    spot.setDistrict(dataNode.getString("District").getValue());
                }
                if (dataNode.containsKey("SurfaceTypeSummer") && !dataNode.isNull("SurfaceTypeSummer")) {
                    spot.setSurfaceType(dataNode.getString("SurfaceTypeSummer").getValue());
                }

                Map<String, Object> servicesMap = new HashMap<>();
                servicesMap.put("raw_data", dataNode.toString());
                spot.setServices(servicesMap);

                if (dataNode.containsKey("geoData") && !dataNode.isNull("geoData")) {
                    try {
                        BsonDocument geoData = dataNode.getDocument("geoData");
                        if (geoData.containsKey("coordinates")) {
                            BsonArray coords = geoData.getArray("coordinates");
                            double lon = getDoubleVal(coords.get(0));
                            double lat = getDoubleVal(coords.get(1));
                            Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
                            spot.setGeom(point);
                        }
                    } catch (Exception e) {
                        log.warn("Ошибка геометрии площадки ID: " + spot.getGlobalId());
                    }
                }

                sportSpotRepository.save(spot);
                count++;
            }
            log.info("Успешно загружено площадок: {}", count);
        } catch (Exception e) {
            log.error("Ошибка при парсинге workouts.bson: ", e);
        }
    }

    private void loadBikeLanes() {
        if (bikeLaneRepository.count() > 0) {
            log.info("Таблица bike_lanes уже заполнена. Пропускаем загрузку.");
            return;
        }
        log.info("Начинаем загрузку велодорожек...");
        try {
            InputStream inputStream = getClass().getResourceAsStream("/bikes.bson");
            if (inputStream == null) {
                log.warn("Файл bikes.bson не найден в resources!");
                return;
            }

            byte[] bytes = inputStream.readAllBytes();
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            BsonBinaryReader reader = new BsonBinaryReader(buffer);
            BsonDocumentCodec codec = new BsonDocumentCodec();

            int count = 0;
            while (buffer.hasRemaining() && reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                BsonDocument doc = codec.decode(reader, DecoderContext.builder().build());
                BikeLane lane = new BikeLane();

                if (doc.containsKey("global_id")) {
                    lane.setGlobalId(doc.getNumber("global_id").longValue());
                } else {
                    lane.setGlobalId((long) count);
                }

                BsonDocument dataNode = doc.containsKey("Cells") ? doc.getDocument("Cells") : doc;

                // Безопасно читаем имя
                if (dataNode.containsKey("Name") && dataNode.get("Name").isString()) {
                    lane.setName(dataNode.getString("Name").getValue());
                }

// Безопасно читаем ширину
                if (dataNode.containsKey("Width") && !dataNode.isNull("Width")) {
                    lane.setLaneWidth(java.math.BigDecimal.valueOf(getDoubleVal(dataNode.get("Width"))));
                }

// 🛠️ Безопасно читаем тип (учитываем, что это может быть массив)
                if (dataNode.containsKey("Type") && !dataNode.isNull("Type")) {
                    if (dataNode.get("Type").isString()) {
                        lane.setLaneType(dataNode.getString("Type").getValue());
                    } else if (dataNode.get("Type").isArray()) {
                        // Если пришел массив из нескольких типов, ставим заглушку или берем сырые данные
                        lane.setLaneType("Смешанный тип");
                    }
                }

                // Парсинг геометрии велодорожки (LineString / MultiLineString)
                if (dataNode.containsKey("geoData") && !dataNode.isNull("geoData")) {
                    try {
                        BsonDocument geoData = dataNode.getDocument("geoData");
                        String type = geoData.getString("type").getValue();
                        BsonArray coordsArray = geoData.getArray("coordinates");

                        if ("LineString".equals(type)) {
                            Coordinate[] coords = new Coordinate[coordsArray.size()];
                            for (int j = 0; j < coordsArray.size(); j++) {
                                BsonArray pt = coordsArray.get(j).asArray();
                                coords[j] = new Coordinate(getDoubleVal(pt.get(0)), getDoubleVal(pt.get(1)));
                            }
                            LineString ls = geometryFactory.createLineString(coords);
                            lane.setGeom(geometryFactory.createMultiLineString(new LineString[]{ls}));
                        } else if ("MultiLineString".equals(type)) {
                            LineString[] lineStrings = new LineString[coordsArray.size()];
                            for (int i = 0; i < coordsArray.size(); i++) {
                                BsonArray lineArray = coordsArray.get(i).asArray();
                                Coordinate[] coords = new Coordinate[lineArray.size()];
                                for (int j = 0; j < lineArray.size(); j++) {
                                    BsonArray pt = lineArray.get(j).asArray();
                                    coords[j] = new Coordinate(getDoubleVal(pt.get(0)), getDoubleVal(pt.get(1)));
                                }
                                lineStrings[i] = geometryFactory.createLineString(coords);
                            }
                            MultiLineString mls = geometryFactory.createMultiLineString(lineStrings);
                            lane.setGeom(mls);
                        }
                    } catch (Exception e) {
                        log.warn("Ошибка геометрии велодорожки ID: " + lane.getGlobalId());
                    }
                }

                bikeLaneRepository.save(lane);
                count++;
            }
            log.info("Успешно загружено велодорожек: {}", count);
        } catch (Exception e) {
            log.error("Ошибка при парсинге bikes.bson: ", e);
        }
    }

    // Безопасный метод для получения чисел (иногда в BSON они лежат как int, иногда как double)
    private double getDoubleVal(org.bson.BsonValue val) {
        if (val.isDouble()) return val.asDouble().getValue();
        if (val.isInt32()) return val.asInt32().getValue();
        if (val.isInt64()) return val.asInt64().getValue();
        return 0.0;
    }
}