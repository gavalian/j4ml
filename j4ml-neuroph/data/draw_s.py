import matplotlib
import numpy as np
import matplotlib.pyplot as plt


matplotlib.rc('xtick', labelsize=14)
matplotlib.rc('ytick', labelsize=14)
matplotlib.rc('axes', labelsize=18)
matplotlib.rc('figure', titlesize=18)

x = np.fromfile('hs.txt',dtype=float,count=-1,sep=' ')
print(x)
# example data
mu = 0.09  # mean of distribution
sigma = 1.689  # standard deviation of distribution
#x = mu + sigma * np.random.randn(437)

num_bins = 120

fig, ax = plt.subplots()

# the histogram of the data
n, bins, patches = ax.hist(x, num_bins, range=[-6.0,6.0],density=True)

# add a 'best fit' line
y = ((1 / (np.sqrt(2 * np.pi) * sigma)) *
     np.exp(-0.5 * (1 / sigma * (bins - mu))**2))
ax.plot(bins, y, '--', color='r')
ax.set_xlabel('Predicted Value - True Value')
ax.set_ylabel('Probability density')
ax.set_title(r'Inference of missing Segment: $\mu=0.09$, $\sigma=1.689$')

# Tweak spacing to prevent clipping of ylabel
fig.tight_layout()
plt.show()
