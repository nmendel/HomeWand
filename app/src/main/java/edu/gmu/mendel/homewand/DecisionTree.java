package edu.gmu.mendel.homewand;

/**
 * Decision tree for deciding what action a Motion object represents
 */
public class DecisionTree {

    // sensor/axis indexes
    public static final int AX = 0;
    public static final int AY = 1;
    public static final int AZ = 2;
    public static final int GX = 3;
    public static final int GY = 4;
    public static final int GZ = 5;

    // constants for all of the possible actions
    public static final String okGoogle = "okgoogle";
    public static final String weather = "weather";
    public static final String disney = "disney";
    public static final String coin = "coin";
    public static final String joke = "joke";
    public static final String schedule = "schedule";
    public static final String news = "news";
    public static final String whatalbum = "whatalbum";
    public static final String soundpet = "soundpet";
    public static final String scarystory = "scarystory";
    public static final String nature = "nature";
    public static final String rockmusic = "rockmusic";
    public static final String delivery = "delivery";
    public static final String stop = "stop";
    public static final String time = "time";
    public static final String twentysideddie = "twentysideddie";
    public static final String timer = "timer";
    public static final String candy = "candy";
    public static final String mickeyadventure = "mickeyadventure";
    public static final String movietimes = "movietimes";


    /**
     * Classify the motion object
     * Coded version of the results of REP Tree from WEKA
     */
    public static String classify(Motion motion) {
        // This is both the most useful and most error prone command, so make it the default
        String result = okGoogle;

        if(motion.DCMean[GZ] < 5.15) {
            if (motion.DCArea[AY] < 650.64) {
                if (motion.DCArea[GY] < 49.75) {
                    if (motion.DCArea[AX] < 30.44) {
                        result = delivery;
                    } else if (motion.DCArea[AX] >= 30.44) {
                        if (motion.DCArea[AZ] < 903.35) {
                            result = coin;
                        } else if (motion.DCArea[AZ] >= 903.35) {
                            result = disney;
                        }
                    }
                } else if (motion.DCArea[GY] >= 49.75) {
                    if (motion.DCArea[AX] < -3.55) {
                        result = weather;
                    } else if (motion.DCArea[AX] >= -3.55) {
                        result = nature;
                    }
                }
            } else if (motion.DCArea[AY] >= 650.64) {
                if (motion.ACLowEnergy[GX] < 75408.97) {
                    if (motion.DCArea[AY] < 1846.04) {
                        result = stop;
                    } else if (motion.DCArea[AY] >= 1846.04) {
                        result = time;
                    }
                } else if (motion.ACLowEnergy[GX] >= 75408.97) {
                    if (motion.DCArea[AX] < 793.85) {
                        result = candy;
                    }
                    if (motion.DCArea[AX] >= 793.85) {
                        result = movietimes;
                    }
                }
            }
        } else if(motion.DCMean[GZ] >= 5.15) {
            if(motion.DCArea[AY] < 98.73) {
                if(motion.ACLowEnergy[AX] < 2481.33) {
                    if(motion.ACIQR[AX] < 0.03) {
                        result = schedule;
                    } else if(motion.ACIQR[AX] >= 0.03) {
                        if(motion.DCArea[AY] < 80.99) {
                            result = joke;
                        } else if(motion.DCArea[AY] >= 80.99) {
                            result = soundpet;
                        }
                    }
                } else if(motion.ACLowEnergy[AX] >= 2481.33) {
                    if(motion.DCArea[AY] < 63.44) {
                        result = whatalbum;
                    }
                    if(motion.DCArea[AY] >= 63.44) {
                        result = news;
                    }
                }
            } else if(motion.DCArea[AY] >= 98.73) {
                if(motion.ACLowEnergy[AZ] < 321730.52) {
                    if(motion.DCArea[GY] < 149.99) {
                        result = scarystory;
                    } else if(motion.DCArea[GY] >= 149.99) {
                        if(motion.DCArea[AZ] < 4037.98) {
                            result = rockmusic;
                        } else if(motion.DCArea[AZ] >= 4037.98) {
                            result = mickeyadventure;
                        }
                    }
                } else if(motion.ACLowEnergy[AZ] >= 321730.52) {
                    if(motion.ACIQR[AX] < 0.11) {
                        result = okGoogle;
                    } else if(motion.ACIQR[AX] >= 0.11) {
                        if(motion.DCArea[AY] < 314.85) {
                            result = timer;
                        } else if(motion.DCArea[AY] >= 314.85) {
                            result = twentysideddie;
                        }
                    }
                }
            }
        }

        return result;
    }
}

/*
REP Tree results from WEKA

=== Run information ===

Scheme:       weka.classifiers.trees.REPTree -M 2 -V 0.001 -N 3 -S 1 -L -1 -I 0.0
Relation:     HomeWand
Instances:    399
Attributes:   71
              DCAreaAX
              DCAreaAY
              DCAreaAZ
              DCAreaGX
              DCAreaGY
              DCAreaGZ
              DCMeanAX
              DCMeanAY
              DCMeanAZ
              DCMeanGX
              DCMeanGY
              DCMeanGZ
              DCTotalMeanA
              DCTotalMeanG
              DCPostureDistAX
              DCPostureDistAY
              DCPostureDistAZ
              DCPostureDistGX
              DCPostureDistGY
              DCPostureDistGZ
              ACLowEnergyAX
              ACLowEnergyAY
              ACLowEnergyAZ
              ACLowEnergyGX
              ACLowEnergyGY
              ACLowEnergyGZ
              ACAbsAreaAX
              ACAbsAreaAY
              ACAbsAreaAZ
              ACAbsAreaGX
              ACAbsAreaGY
              ACAbsAreaGZ
              ACTotalAbsAreaA
              ACTotalAbsAreaG
              ACAbsMeanAX
              ACAbsMeanAY
              ACAbsMeanAZ
              ACAbsMeanGX
              ACAbsMeanGY
              ACAbsMeanGZ
              ACEnergyAX
              ACEnergyAY
              ACEnergyAZ
              ACEnergyGX
              ACEnergyGY
              ACEnergyGZ
              ACVarAX
              ACVarAY
              ACVarAZ
              ACVarGX
              ACVarGY
              ACVarGZ
              ACAbsCVAX
              ACAbsCVAY
              ACAbsCVAZ
              ACAbsCVGX
              ACAbsCVGY
              ACAbsCVGZ
              ACIQRAX
              ACIQRAY
              ACIQRAZ
              ACIQRGX
              ACIQRGY
              ACIQRGZ
              ACRangeAX
              ACRangeAY
              ACRangeAZ
              ACRangeGX
              ACRangeGY
              ACRangeGZ
              Class
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===


REPTree
============

DCMeanGZ < 5.15
|   DCAreaAY < 650.64
|   |   DCAreaGY < 49.75
|   |   |   DCAreaAX < 30.44 : delivery (13/0) [7/1]
|   |   |   DCAreaAX >= 30.44
|   |   |   |   DCAreaAZ < 903.35 : coin (13/0) [8/1]
|   |   |   |   DCAreaAZ >= 903.35 : disney (13/0) [6/1]
|   |   DCAreaGY >= 49.75
|   |   |   DCAreaAX < -3.55 : weather (13/0) [7/0]
|   |   |   DCAreaAX >= -3.55 : nature (14/0) [6/0]
|   DCAreaAY >= 650.64
|   |   ACLowEnergyGX < 75408.97
|   |   |   DCAreaAY < 1846.04 : stop (14/0) [8/2]
|   |   |   DCAreaAY >= 1846.04 : time (13/0) [8/1]
|   |   ACLowEnergyGX >= 75408.97
|   |   |   DCAreaAX < 793.85 : candy (13/0) [6/0]
|   |   |   DCAreaAX >= 793.85 : movieTimes (14/0) [6/0]
DCMeanGZ >= 5.15
|   DCAreaAY < 98.73
|   |   ACLowEnergyAX < 2481.33
|   |   |   ACIQRAX < 0.03 : schedule (14/0) [6/0]
|   |   |   ACIQRAX >= 0.03
|   |   |   |   DCAreaAY < 80.99 : joke (13/0) [11/4]
|   |   |   |   DCAreaAY >= 80.99 : soundpet (13/0) [6/0]
|   |   ACLowEnergyAX >= 2481.33
|   |   |   DCAreaAY < 63.44 : whatalbum (14/0) [11/5]
|   |   |   DCAreaAY >= 63.44 : news (13/0) [4/0]
|   DCAreaAY >= 98.73
|   |   ACLowEnergyAZ < 321730.52
|   |   |   DCAreaGY < 149.99 : scarystory (14/0) [7/1]
|   |   |   DCAreaGY >= 149.99
|   |   |   |   DCAreaAZ < 4037.98 : rockMusic (13/0) [7/0]
|   |   |   |   DCAreaAZ >= 4037.98 : mickeyAdventure (13/0) [4/1]
|   |   ACLowEnergyAZ >= 321730.52
|   |   |   ACIQRAX < 0.11 : okGoogle (13/0) [5/0]
|   |   |   ACIQRAX >= 0.11
|   |   |   |   DCAreaAY < 314.85 : timer (14/0) [3/0]
|   |   |   |   DCAreaAY >= 314.85 : twentySidedDie (12/0) [7/2]

Size of the tree : 39

Time taken to build model: 0.02 seconds

=== Stratified cross-validation ===
=== Summary ===

Correctly Classified Instances         361               90.4762 %
Incorrectly Classified Instances        38                9.5238 %
Kappa statistic                          0.8997
Mean absolute error                      0.0121
Root mean squared error                  0.0936
Relative absolute error                 12.7143 %
Root relative squared error             42.9663 %
Total Number of Instances              399

=== Detailed Accuracy By Class ===

                 TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0.700    0.016    0.700      0.700    0.700      0.684    0.911     0.635     okGoogle
                 0.950    0.005    0.905      0.950    0.927      0.923    0.974     0.943     weather
                 0.950    0.008    0.864      0.950    0.905      0.901    0.971     0.828     disney
                 0.950    0.003    0.950      0.950    0.950      0.947    0.973     0.900     coin
                 0.800    0.000    1.000      0.800    0.889      0.890    0.947     0.895     joke
                 1.000    0.011    0.833      1.000    0.909      0.908    0.997     0.885     schedule
                 1.000    0.003    0.952      1.000    0.976      0.975    0.999     0.985     news
                 0.950    0.000    1.000      0.950    0.974      0.973    1.000     0.992     whatalbum
                 0.900    0.005    0.900      0.900    0.900      0.895    0.972     0.904     soundpet
                 0.900    0.016    0.750      0.900    0.818      0.811    0.941     0.697     scarystory
                 0.950    0.000    1.000      0.950    0.974      0.973    1.000     0.993     nature
                 0.850    0.000    1.000      0.850    0.919      0.918    0.975     0.943     rockMusic
                 0.950    0.000    1.000      0.950    0.974      0.973    1.000     0.995     delivery
                 0.900    0.003    0.947      0.900    0.923      0.919    0.948     0.836     stop
                 0.900    0.008    0.857      0.900    0.878      0.872    0.973     0.900     time
                 0.842    0.011    0.800      0.842    0.821      0.812    0.936     0.757     twentySidedDie
                 0.850    0.005    0.895      0.850    0.872      0.866    0.920     0.786     timer
                 0.900    0.008    0.857      0.900    0.878      0.872    0.946     0.807     candy
                 0.900    0.000    1.000      0.900    0.947      0.946    0.999     0.984     mickeyAdventure
                 0.950    0.000    1.000      0.950    0.974      0.973    0.974     0.953     movieTimes
Weighted Avg.    0.905    0.005    0.911      0.905    0.906      0.902    0.968     0.881

=== Confusion Matrix ===

  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t   <-- classified as
 14  0  0  0  0  0  0  0  1  4  0  0  0  0  0  1  0  0  0  0 |  a = okGoogle
  0 19  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  1  0  0 |  b = weather
  0  0 19  1  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0 |  c = disney
  0  0  1 19  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0 |  d = coin
  0  0  0  0 16  3  0  0  1  0  0  0  0  0  0  0  0  0  0  0 |  e = joke
  0  0  0  0  0 20  0  0  0  0  0  0  0  0  0  0  0  0  0  0 |  f = schedule
  0  0  0  0  0  0 20  0  0  0  0  0  0  0  0  0  0  0  0  0 |  g = news
  0  0  0  0  0  0  1 19  0  0  0  0  0  0  0  0  0  0  0  0 |  h = whatalbum
  0  1  0  0  0  0  0  0 18  1  0  0  0  0  0  0  0  0  0  0 |  i = soundpet
  1  0  0  0  0  1  0  0  0 18  0  0  0  0  0  0  0  0  0  0 |  j = scarystory
  0  1  0  0  0  0  0  0  0  0 19  0  0  0  0  0  0  0  0  0 |  k = nature
  2  0  0  0  0  0  0  0  0  1  0 17  0  0  0  0  0  0  0  0 |  l = rockMusic
  0  0  1  0  0  0  0  0  0  0  0  0 19  0  0  0  0  0  0  0 |  m = delivery
  0  0  0  0  0  0  0  0  0  0  0  0  0 18  1  0  1  0  0  0 |  n = stop
  0  0  0  0  0  0  0  0  0  0  0  0  0  1 18  0  0  1  0  0 |  o = time
  1  0  1  0  0  0  0  0  0  0  0  0  0  0  0 16  1  0  0  0 |  p = twentySidedDie
  2  0  0  0  0  0  0  0  0  0  0  0  0  0  0  1 17  0  0  0 |  q = timer
  0  0  0  0  0  0  0  0  0  0  0  0  0  0  2  0  0 18  0  0 |  r = candy
  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  2  0  0 18  0 |  s = mickeyAdventure
  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  1  0 19 |  t = movieTimes


 */