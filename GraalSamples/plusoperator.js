Polyglot.evalFile('python', 'plusoperator.py');

var plus = Polyglot.import('plus');

console.log(plus(3, 2));
console.log(plus("hello", " world"));