package com.example.ChessPlayerApp.chessboardcamera.KmeanCluster;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class KMeans {

    private static final Random random = new Random();

    public static Map<Centroid, List<Line>> fit(List<Line> lines,
                                                int k,
                                                Distance distance,
                                                int maxIterations){
        List<Centroid> centroids =  distantCentroids(lines, k, distance);
        Map<Centroid, List<Line>> clusters = new HashMap<>();
        Map<Centroid, List<Line>> lastState = new HashMap<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            for (Line line : lines) {
                Centroid centroid = nearestCentroid(line, centroids, distance);
                assignToCluster(clusters, line, centroid);
            }

            // if the assignments do not change, then the algorithm terminates
            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            // at the end of each iteration we should relocate the centroids
            centroids = relocateCentroids(clusters);
            clusters = new HashMap<>();
        }

        return lastState;
    }

    public static Map<Centroid, List<Line>> fit2(List<Line> lines,
                                                double minSplitDistance,
                                                Distance distance,
                                                int maxIterations){
        List<Centroid> centroids =  distantCentroids2(lines, minSplitDistance, distance);
        Map<Centroid, List<Line>> clusters = new HashMap<>();
        Map<Centroid, List<Line>> lastState = new HashMap<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            for (Line line : lines) {
                Centroid centroid = nearestCentroid(line, centroids, distance);
                assignToCluster(clusters, line, centroid);
            }

            // if the assignments do not change, then the algorithm terminates
            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            // at the end of each iteration we should relocate the centroids
            centroids = relocateCentroids(clusters);
            clusters = new HashMap<>();
        }

        return lastState;
    }

    public static Map<Centroid, List<LineWithPoint>> fit2_point(List<LineWithPoint> lineWithPoints, int minSplitDistance, PointDistance pointDistance, int maxIterations) {
        List<Point> allPoints = new ArrayList<>();
        for(LineWithPoint lp: lineWithPoints)
            allPoints.add(lp.point);

        // only use point field of Centroid
        List<Centroid> centroids =  distantCentroids2_Point(allPoints, minSplitDistance, pointDistance);
        /*
        for(Centroid ce: centroids){
            if (ce == null){
                Log.d("CentroidNl", "Null cen in line 92 of KMean");
            }
        }

         */

        Map<Centroid, List<LineWithPoint>> clusters = new HashMap<>();

        Map<Centroid, List<LineWithPoint>> lastState = new HashMap<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {
            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            for (LineWithPoint lp : lineWithPoints) {
                Centroid centroid = nearestCentroid(lp.point, centroids, pointDistance);
                /*
                if (centroid == null) {
                    Log.d("CentroidNl", "Null cen in line 108 of KMean");
                    Log.d("CentroidNl", "how many centroids: " + centroids.size());
                }

                 */

                assignToCluster(clusters, lp, centroid);
            }

            // if the assignments do not change, then the algorithm terminates
            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);
            lastState = clusters;
            if (shouldTerminate) {
                break;
            }

            // at the end of each iteration we should relocate the centroids
            centroids = relocateCentroids_LP(clusters);
            clusters = new HashMap<>();
        }

        return lastState;
    }




    private static List<Centroid> distantCentroids(List<Line> lines, int k, Distance distance){
        List<Centroid> centroids = new ArrayList<>();

        for(int i = 0; i < k; i ++){
            if (i == 0){
                // first Centroid
                Centroid c = new Centroid(lines.get(0).getRtho(), lines.get(0).getTheta());
                centroids.add(c);
                //lines.remove(0);
                continue;
            }
            // find the largest
            double largestDistToAllCent = Double.MIN_VALUE;
            Line nextCen = lines.get(0);
            for(Line line: lines){
                double avgDist = minDistanceToAllCent(line, centroids, distance);
                if(avgDist > largestDistToAllCent){
                    largestDistToAllCent = avgDist;
                    nextCen = line;
                }
            }
            centroids.add(new Centroid(nextCen.getRtho(), nextCen.getTheta()));
            //lines.remove(nextCen);

        }

        return centroids;

    }

    private static List<Centroid> distantCentroids2(List<Line> lines, double minDist, Distance distance){
        List<Centroid> centroids = new ArrayList<>();

        // maximum distance from sample to all centroid
        double maxDist = Double.MAX_VALUE;

        int k = 1;

        // first Centroid
        Centroid c = new Centroid(lines.get(0).getRtho(), lines.get(0).getTheta());
        centroids.add(c);
        Line nextCen = lines.get(0);

        // when we can't find maxDist which is greater than minDist, split stops
        do{

            // find the largest
            double largestDistToAllCent = Double.MIN_VALUE;

            for(Line line: lines){
                double avgDist = minDistanceToAllCent(line, centroids, distance);
                if(avgDist > largestDistToAllCent){
                    largestDistToAllCent = avgDist;
                    nextCen = line;
                }
            }

            if(largestDistToAllCent < maxDist)
                maxDist = largestDistToAllCent;

            if(maxDist >= minDist) {
                centroids.add(new Centroid(nextCen.getRtho(), nextCen.getTheta()));
                k ++;
            }
            //lines.remove(nextCen);


        }while(maxDist >= minDist);

        return centroids;

    }

    private static List<Centroid> distantCentroids2_Point(List<Point> points, double minDist, Distance distance){
        List<Centroid> centroids = new ArrayList<>();

        // maximum distance from sample to all centroid
        double maxDist = Double.MAX_VALUE;

        int k = 1;

        // first Centroid
        Centroid c = new Centroid(points.get(0));
        centroids.add(c);

        Point nextCen = points.get(0);
        // when we can't find maxDist which is greater than minDist, split stops
        do{

            // find the largest
            double largestDistToAllCent = Double.MIN_VALUE;

            for(Point p: points){
                double avgDist = minDistanceToAllCent(p, centroids, distance);
                if(avgDist > largestDistToAllCent){
                    largestDistToAllCent = avgDist;
                    nextCen = p;
                }
            }

            if(largestDistToAllCent < maxDist)
                maxDist = largestDistToAllCent;

            if(maxDist >= minDist) {
                centroids.add(new Centroid(nextCen));
                k ++;
            }
            //lines.remove(nextCen);


        }while(maxDist >= minDist);

        return centroids;

    }


    private static double minDistanceToAllCent(Line line, List<Centroid> centroids, Distance distance) {
        double minDist = Double.MAX_VALUE;
        for(Centroid cent: centroids){
            double dist = distance.calculate(line, cent.line);
            if( dist < minDist){
                minDist = dist;
            }
        }

        return minDist;
    }

    private static double minDistanceToAllCent(Point point, List<Centroid> centroids, Distance distance) {
        double minDist = Double.MAX_VALUE;
        for(Centroid cent: centroids){
            double dist = distance.calculate(point, cent.point);
            if( dist < minDist){
                minDist = dist;
            }
        }

        return minDist;
    }

    private static List<Centroid> randomCentroids(List<Line> lines, int k){
        List<Centroid> centroids = new ArrayList<>();
        // max[0]: rtho, max[1]: theta
        double[] max = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double[] min = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};

        for (Line line : lines){
            if (line.getRtho() > max[0])
                max[0] = line.getRtho();
            else if (line.getRtho() < min[0])
                min[0] = line.getRtho();

            if (line.getTheta() > max[1])
                max[1] = line.getTheta();
            else if (line.getTheta() < min[1])
                min[1] = line.getTheta();
        }

        for (int i = 0; i < k; i ++){
            centroids.add(new Centroid(random.nextDouble()*(max[0] - min[0]) + min[0], random.nextDouble()*(max[1] - min[1]) + min[1]));
        }

        return centroids;
    }

    private static Centroid nearestCentroid(Line line, List<Centroid> centroids, Distance distance){
        double minimumDistance = Double.MAX_VALUE;
        Centroid nearest = null;

        for (Centroid centroid : centroids){
            double currentDistance = distance.calculate(line, centroid.line);

            if (currentDistance < minimumDistance){
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }
        return nearest;
    }

    private static Centroid nearestCentroid(Point p, List<Centroid> centroids, Distance distance){
        double minimumDistance = Double.MAX_VALUE;
        Centroid nearest = centroids.get(0);

        for (Centroid centroid : centroids){
            double currentDistance = distance.calculate(p, centroid.point);

            if (currentDistance < minimumDistance){
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }
        return nearest;
    }

    private static void assignToCluster(Map<Centroid, List<Line>> clusters,
                                         final Line line,
                                         Centroid centroid){
        List<Line> list = clusters.get(centroid);
        if (list == null){
            list = new ArrayList<Line>();
            clusters.put(centroid, list);
        }
        list.add(line);
    }

    private static void assignToCluster(Map<Centroid, List<LineWithPoint>> clusters,
                                        final LineWithPoint lineWithPoint,
                                        Centroid centroid) {
        List<LineWithPoint> list = clusters.get(centroid);
        if (list == null){
            list = new ArrayList<>();
            clusters.put(centroid, list);
        }
        list.add(lineWithPoint);
    }




    private static boolean inRangeWithPeriod(double number, double min, double max, double period){
        while(number - min > period){
            number -= period;
        }

        while(max - number > period){
            number += period;
        }

        if (number <= max && number >= min)
            return true;
        else{
            return false;
        }

    }

    private static Centroid average(Centroid centroid, List<Line> lines){
        if (lines == null || lines.isEmpty()){
            return centroid;
        }
        double rtho_tot = 0;
        double theta_tot = 0;


        // turn to false when find two different theta
        boolean first = true;
        double theta_standard = 0;

        for (Line line : lines){
            rtho_tot += line.getRtho();

            if(first){
                first = false;
                theta_standard = line.getTheta();
                theta_tot += theta_standard;
                continue;
            }

            double distToSt = line.getTheta() - theta_standard;
            if(distToSt > 90) {
                theta_tot += line.getTheta() - 180;
            }else if(distToSt < -90) {
                theta_tot += line.getTheta() + 180;
            }else {
                theta_tot += line.getTheta();
            }

        }

        return new Centroid(rtho_tot/lines.size(), theta_tot/lines.size());
    }

    private static Centroid average_lp(Centroid centroid, List<LineWithPoint> lps){
        if (lps == null || lps.isEmpty()){
            return centroid;
        }
        double x_tot = 0;
        double y_tot = 0;


        for(LineWithPoint lp : lps){
            x_tot += lp.point.x;
            y_tot += lp.point.y;
        }

        return new Centroid(new Point(x_tot/lps.size(), y_tot/lps.size()));
    }

    private static List<Centroid> relocateCentroids(Map<Centroid, List<Line>> clusters) {
        //return clusters.entrySet().stream().map(e -> average(e.getKey(), e.getValue())).collect(toList());
        List<Centroid> newCentroid = new ArrayList<>();
        for(Centroid centroid: clusters.keySet()){
            newCentroid.add(average(centroid, clusters.get(centroid)));
        }
        return newCentroid;
    }

    private static List<Centroid> relocateCentroids_LP(Map<Centroid, List<LineWithPoint>> clusters) {
        //return clusters.entrySet().stream().map(e -> average(e.getKey(), e.getValue())).collect(toList());
        List<Centroid> newCentroid = new ArrayList<>();
        for(Centroid centroid: clusters.keySet()){
            newCentroid.add(average_lp(centroid, clusters.get(centroid)));
        }
        return newCentroid;
    }



}
