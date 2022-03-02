
var myArray = [42, 1337, 360, 12, 123456]

Polyglot.export('tableau', myArray)
Polyglot.evalFile('python', 'arraysort.py')

console.log("Displaying the array in JS again after it was 'sorted' in python :")
console.log(myArray) 

