
import os

commands = ['okGoogle', 'weather']

for s in commands:
    cmd = 'platform-tools-latest-windows\\platform-tools\\adb.exe -s ZX1F222566 shell "run-as edu.gmu.mendel.homewand cat /data/data/edu.gmu.mendel.homewand/files/%s/signature.arff" > signatures\\%s.arff' % (s, s)
    print cmd
    os.system(cmd)
