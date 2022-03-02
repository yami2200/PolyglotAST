
Polyglot.eval('python', 'x = [7, 3, 12, 42, 88, 9]');
Polyglot.eval('python', 'x.sort()');

var answer = Polyglot.eval('python', 'x[4]');
console.log(answer);

