package com.example.proyectoequipoahorasi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_PICTURES = 1;
    private List<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Iniciar el intent de la galería para seleccionar múltiples imágenes
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Selecciona imágenes"), SELECT_PICTURES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURES && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            bitmaps.add(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        bitmaps.add(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Ahora crear el mosaico con las imágenes seleccionadas
                if (!bitmaps.isEmpty()) {
                    Bitmap mosaic = crearMosaico(bitmaps, 595, 842, 100);
                    guardarImagen(mosaic);
                }
            }
        }
    }

    // Función para obtener el color dominante en un bitmap
//    public int getDominantColor(Bitmap bitmap) {
//        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
//        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        HashMap<Integer, Integer> colorMap = new HashMap<>();
//        for (int pixel : pixels) {
//            if (colorMap.containsKey(pixel)) {
//                colorMap.put(pixel, colorMap.get(pixel) + 1);
//            } else {
//                colorMap.put(pixel, 1);
//            }
//        }
//
//        int dominantColor = 0;
//        int maxCount = 0;
//        for (Map.Entry<Integer, Integer> entry : colorMap.entrySet()) {
//            if (entry.getValue() > maxCount) {
//                dominantColor = entry.getKey();
//                maxCount = entry.getValue();
//            }
//        }
//        return dominantColor;
//    }

    // Función para crear un mosaico con las imágenes seleccionadas
    public Bitmap crearMosaico(List<Bitmap> bitmaps, int anchoMosaico, int altoMosaico, int tamanoCelda) {
        // Crear un lienzo para el mosaico
        Bitmap mosaico = Bitmap.createBitmap(anchoMosaico, altoMosaico, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mosaico);

        int filas = altoMosaico / tamanoCelda;
        int columnas = anchoMosaico / tamanoCelda;

        // Iterar sobre las filas y columnas del mosaico
        int idx = 0;
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                if (idx >= bitmaps.size()) idx = 0;  // Repetir imágenes si se acaban
                Bitmap imagenEscalada = Bitmap.createScaledBitmap(bitmaps.get(idx), tamanoCelda, tamanoCelda, false);
                canvas.drawBitmap(imagenEscalada, x * tamanoCelda, y * tamanoCelda, null);
                idx++;
            }
        }

        return mosaico;
    }

    // Función para guardar la imagen final del mosaico
    public void guardarImagen(Bitmap bitmap) {
        File file = new File(getExternalFilesDir(null), "mosaico.png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "Imagen guardada en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
