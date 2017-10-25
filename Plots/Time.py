import pandas as pd
from matplotlib import pyplot as plt
import seaborn as sns

sns.set(style="darkgrid")
df = pd.read_csv('/Users/chelseametcalf/eclipse-workspace/CAP5510/topData.csv')

g = sns.FacetGrid(df, hue="AlignmentMethod", size=8)
g.map(plt.scatter, "QueryLength", "Time")
g.map(plt.plot, "QueryLength", "Time")
plt.savefig("length_vs_time.pdf")