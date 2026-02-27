import 'package:flutter/material.dart';
import 'package:yandex_mapkit/yandex_mapkit.dart';
import 'package:geolocator/geolocator.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MaterialApp(home: MapScreen()));
}

class MapScreen extends StatefulWidget {
  const MapScreen({Key? key}) : super(key: key);

  @override
  State<MapScreen> createState() => _MapScreenState();
}

class _MapScreenState extends State<MapScreen> {
  late YandexMapController mapController;
  bool isMapReady = false;

  // Метод для запроса прав и получения локации
  Future<void> _moveToCurrentLocation() async {
    // Запрашиваем права
    var permissionStatus = await Permission.location.request();

    if (permissionStatus.isGranted) {
      // Получаем текущую позицию
      Position position = await Geolocator.getCurrentPosition(
          desiredAccuracy: LocationAccuracy.high);

      // Двигаем камеру
      if (isMapReady) {
        mapController.moveCamera(
          CameraUpdate.newCameraPosition(
            CameraPosition(
              target: Point(
                latitude: position.latitude,
                longitude: position.longitude,
              ),
              zoom: 15.0, // Приближение
            ),
          ),
          animation: const MapAnimation(
            type: MapAnimationType.smooth,
            duration: 1.5, // Плавный полет к точке
          ),
        );
      }
    } else {
      // Здесь можно показать Snackbar, что без прав геолокация не работает
      debugPrint('Права на геолокацию не выданы');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Зеленый маршрутизатор')),
      body: YandexMap(
        onMapCreated: (controller) {
          mapController = controller;
          isMapReady = true;
          _moveToCurrentLocation();
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _moveToCurrentLocation,
        child: const Icon(Icons.my_location),
      ),
    );
  }
}