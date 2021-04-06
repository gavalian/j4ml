import numpy as np
import matplotlib.pyplot as plt
import matplotlib

matplotlib.rcParams['figure.figsize'] = 9, 3
x, y = np.loadtxt("raw/dc_hits_21.txt",unpack=True)
xt, yt = np.loadtxt("raw/dc_track_21.txt",unpack=True)
colors = (0,0,0)
area = np.pi*10

# Plot
plt.scatter(y, x, s=area, c=colors, alpha=0.5)
plt.scatter(yt, xt, s=area, color='red', alpha=0.5)
#plt.figure(figsize=(20,10))
plt.title('DC Layer vs Wire Number')
plt.xlim(0.0,112.0)
plt.ylim(0.0,36.0)
plt.yticks(ticks=[6,12,18,24,30,36])
#plt.xlabel('Wire Number')
plt.ylabel('Drift Chamber Layer')
plt.grid(axis='y',linestyle='--')
plt.show()
