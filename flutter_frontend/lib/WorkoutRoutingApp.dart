import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_compass/flutter_compass.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_map_location_marker/flutter_map_location_marker.dart';
import 'package:geolocator/geolocator.dart';
import 'package:http/http.dart' as http;
import 'package:latlong2/latlong.dart';
class MapScreen extends StatefulWidget {
  const MapScreen({super.key});

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  final MapController _mapController = MapController();
  double? _currentHeading;

  // 1. Создаем пустой список для наших турников
  List<Marker> _sportMarkers = [];

  @override
  void initState() {
    super.initState();
    _requestLocationPermission();
    FlutterCompass.events?.listen((event) {
      if (mounted) {
        setState(() {
          _currentHeading = event.heading;
        });
      }
    });

    // Загружаем площадки вокруг центра Москвы при старте приложения
    _fetchSportSpots(55.7558, 37.6173);
  }

  // 2. ФУНКЦИЯ ОБЩЕНИЯ СО SPRING BOOT
  Future<void> _fetchSportSpots(double lat, double lon) async {
    // Используем 10.0.2.2 для эмулятора Android (если реальный телефон - нужен IP компа по Wi-Fi)
    final url = Uri.parse('http://10.0.2.2:8080/api/spots/nearby?lat=$lat&lon=$lon&radius=5000');

    try {
      final response = await http.get(url);

      if (response.statusCode == 200) {
        // Читаем JSON в правильной кодировке (чтобы русские буквы не сломались)
        final List<dynamic> data = json.decode(utf8.decode(response.bodyBytes));

        setState(() {
          // Превращаем каждый объект из БД в Marker для карты
          _sportMarkers = data.map((spot) {

            // ВНИМАНИЕ: Если твои поля в DTO на бэкенде называются иначе (например x и y),
            // поменяй слова 'lat' и 'lon' ниже на свои!
            double spotLat = spot['lat'];
            double spotLon = spot['lon'];

            return Marker(
              point: LatLng(spotLat, spotLon),
              width: 40,
              height: 40,
              child: const Icon(
                Icons.fitness_center, // Иконка гантели
                color: Colors.redAccent,
                size: 30,
              ),
            );
          }).toList();
        });
      } else {
        debugPrint("Ошибка сервера: ${response.statusCode}");
      }
    } catch (e) {
      debugPrint("Ошибка сети: $e");
    }
  }

  Future<void> _requestLocationPermission() async {
    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      await Geolocator.requestPermission();
    }
  }

  Future<void> _moveToCurrentLocation() async {
    try {
      Position position = await Geolocator.getCurrentPosition();

      _mapController.move(
        LatLng(position.latitude, position.longitude),
        16.0,
      );
      _mapController.rotate(0);

      // 3. Загружаем площадки вокруг пользователя, когда он нажал "найти меня"
      _fetchSportSpots(position.latitude, position.longitude);

    } catch (e) {
      debugPrint("Ошибка навигации: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          FlutterMap(
            mapController: _mapController,
            options: MapOptions(
              initialCenter: const LatLng(55.7558, 37.6173),
              initialZoom: 13.0,
              interactionOptions: const InteractionOptions(
                flags: InteractiveFlag.all & ~InteractiveFlag.rotate,
              ),
            ),
            children: [
              TileLayer(
                urlTemplate: 'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png',
                subdomains: const ['a', 'b', 'c', 'd'],
                userAgentPackageName: 'com.example.workout_app',
                keepBuffer: 3,
                panBuffer: 1,
              ),

              // 4. ДОБАВЛЯЕМ СЛОЙ С МАРКЕРАМИ ПОВЕРХ КАРТЫ
              MarkerLayer(
                markers: _sportMarkers,
              ),

              CurrentLocationLayer(
                alignPositionOnUpdate: AlignOnUpdate.never,
                alignDirectionOnUpdate: AlignOnUpdate.never,
                style: const LocationMarkerStyle(
                  marker: DefaultLocationMarker(color: Colors.blueAccent),
                  markerDirection: MarkerDirection.heading,
                ),
              ),
            ],
          ),

          // Поисковая панель
          Positioned(
            top: 50,
            left: 20,
            right: 20,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 15),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                boxShadow: [
                  BoxShadow(color: Colors.black.withOpacity(0.08), blurRadius: 15, offset: const Offset(0, 5)),
                ],
              ),
              child: const Row(
                children: [
                  Icon(Icons.directions_run, color: Colors.black87),
                  SizedBox(width: 15),
                  Text('Поиск маршрута...', style: TextStyle(fontSize: 16, color: Colors.black54)),
                ],
              ),
            ),
          ),

          // Кнопка локации
          Positioned(
            bottom: 40,
            right: 20,
            child: FloatingActionButton(
              backgroundColor: Colors.black87,
              onPressed: _moveToCurrentLocation,
              child: const Icon(Icons.my_location, color: Colors.white),
            ),
          ),
        ],
      ),
    );
  }
}


