package com.company;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.*;

public class Main {

    //Funkcja zwraca przekształconą macierz, zawierającą bardziej kontrastowy obraz, co pozwala na lepszą jego analizę
    public static Mat przeksztalcObraz(Mat macierz) {

        Mat hsv = new Mat();
        Mat temp = new Mat();

        threshold(macierz, macierz, 80, 255, THRESH_BINARY); //cz-b
        medianBlur(macierz, macierz, 21); // rozmazuje

        for (int i = 0; i < macierz.width(); i++)
            for (int j = 0; j < macierz.height(); j++) {
                double[] data2 = macierz.get(j, i);
                double[] data = {0, 255, 0};
                if (data2[0] == 0)
                    macierz.put(j, i, data);
            }


        cvtColor(macierz, hsv, COLOR_RGB2HSV);
        Core.inRange(hsv, new Scalar(50, 100, 0), new Scalar(95, 255, 255), temp);
        return temp;
    }

    //Funkcja jako parametr przyjmuje znalezione okręgi na obrazie (kola) i zwraca id kola o najmniejszym promienu (kolo centralne)
    public static int minimalnyOkrag(Mat kola) {

        int minimalny_okrag = 0;
        for (int i = 0; i < kola.cols(); i++) {
            double[] vKola = kola.get(0, i);
            int radius = (int) Math.round(vKola[2]);

            if (i == 0)
                minimalny_okrag = i;
            else if (radius < kola.get(0, minimalny_okrag)[2])
                minimalny_okrag = i;

        }
        return minimalny_okrag;
    }

    //Funkcja jako parametr przyjmuje przekształconą macierz, oraz wspolrzedne centralnego kola, a następnie zwraca wielkość drugiego od srodka promienia kola;
    //Funkcja bazuje na liczeniu kontrastowych zmian, zachodzących na granicy okregow, w celu ustalenia wielkosci szukanego promienia
    public static int wyznaczPromienDrugiegoOkregu(Mat macierz, Point pt) {
        int ilosc = 0;
        int x1 = 0;
        int licznik = 0;

        while (ilosc < 4) {
            licznik++;
            if (macierz.get((int) (pt.x + licznik), (int) (pt.y))[0] != macierz.get((int) (pt.x + licznik + 1), (int) (pt.y))[0])
                ilosc++;
            x1 = licznik;
        }
        return x1;

    }

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Imgcodecs imageCodecs = new Imgcodecs();

        //WCZYTANIE ZDJECIA I ZAPIS DO MATRIXA
        String plik = "in.png";
        String plik2 = "out.png";
        Mat macierz = imageCodecs.imread(plik);
        Mat macierz2 = new Mat();
        macierz.copyTo(macierz2);
        Mat kola = new Mat();

        //WYSZUKIWANIE OKRĘGÓW I ZAPISANIE INFORMACJI O NICH DO MACIERZY "kola"
        HoughCircles(przeksztalcObraz(macierz), kola, CV_HOUGH_GRADIENT, 2, 1, 100, 180, 70, 170);

        //  WYZNACZENIE SRODKA CENTRALNEGO OKREGU
        Point pt = new Point(Math.round(kola.get(0, minimalnyOkrag(kola))[0]), Math.round(kola.get(0, minimalnyOkrag(kola))[1]));

        //ZAZNACZENIE OKREGU NA OBRAZIE
        Imgproc.circle(macierz2, new Point(pt.x, pt.y), wyznaczPromienDrugiegoOkregu(macierz, pt), new Scalar(0, 0, 255), 13);

        //ZAPISANIE PLIKU
        imageCodecs.imwrite(plik2, macierz2);

    }
}