Polyglot.evalFile('python', 'objectexample.py');

var john = Polyglot.import('John');
var test = john.foo();

console.log("Displaying John in JS :");
console.log(john);
console.log(john.__str__());

john.age = 43;

console.log("Displaying John in python again after changing a field in JS : ");

Polyglot.eval('python', 'print(test)');