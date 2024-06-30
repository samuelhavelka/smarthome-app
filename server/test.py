import numpy as np

test = np.random.normal(0.5,0.2,20).round(3)
numbers = []
for i in test:
    if i > 0.1 and i < 0.9: numbers.append(i)
numbers = sorted(list(set(numbers)))

print(numbers)