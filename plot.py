import matplotlib.pyplot as plt

with open('results', 'r') as file:
    lines = file.readlines()

float_lists = []

for line in lines:
    float_list = [float(x) for x in line.split()]
    float_lists.append(float_list) # rhos, eNum, eTime, eProbBlock, aNum, aTime, aProbBlock

rhos = float_lists[0]
eNum = float_lists[1]
eTime = float_lists[2]
eProbBlock = float_lists[3]
eUtil = float_lists[4]
aNum = float_lists[5]
aTime = float_lists[6]
aProbBlock = float_lists[7]
aUtil = float_lists[8]

fig, ax = plt.subplots()

ax.scatter(rhos, eNum, label='Expected Number of Customers', color='red', marker='o')
ax.scatter(rhos, aNum, label='Actual Number of Customers', color='blue', marker='s')
ax.set_xlabel('ρ')
ax.set_ylabel('E[N]')
ax.legend()

plt.show()

fig, ax = plt.subplots()

ax.scatter(rhos, eTime, label='Expected Time through System per Customer', color='red', marker='o')
ax.scatter(rhos, aTime, label='Actual Time through System per Customer', color='blue', marker='s')
ax.set_xlabel('ρ')
ax.set_ylabel('E[T]')
ax.legend()

plt.show()

fig, ax = plt.subplots()

ax.scatter(rhos, eProbBlock, label='Expected Blocking Probability', color='red', marker='o')
ax.scatter(rhos, aProbBlock, label='Actual Blocking Probability', color='blue', marker='s')
ax.set_xlabel('ρ')
ax.set_ylabel('P(block)')
ax.legend()

plt.show()

fig, ax = plt.subplots()

ax.scatter(rhos, eUtil, label='Expected Utilization', color='red', marker='o')
ax.scatter(rhos, aUtil, label='Actual Utilization', color='blue', marker='s')
ax.set_xlabel('ρ')
ax.set_ylabel('U')
ax.legend()

plt.show()