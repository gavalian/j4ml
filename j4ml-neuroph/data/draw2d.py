import numpy as np
import matplotlib.pyplot as plt
import random

y, x = np.loadtxt("h2.txt",unpack=True)

print (y)
plt.hist2d(x,y,bins=[120,6],range=[[-6.0, 6.0], [0, 5]])
plt.colorbar()
plt.show()
