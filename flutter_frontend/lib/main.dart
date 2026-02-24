import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import 'WorkoutRoutingApp.dart';

void main() {
  // Гарантируем инициализацию плагинов перед запуском приложения
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const WorkoutRoutingApp());
}

class WorkoutRoutingApp extends StatelessWidget {
  const WorkoutRoutingApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MosWorkout',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.black),
        useMaterial3: true,
        fontFamily: 'Roboto',
      ),
      home: const MapScreen(),
    );
  }
}