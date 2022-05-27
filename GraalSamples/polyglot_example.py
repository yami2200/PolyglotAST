import polyglot

polyglot.export_value(name="test", value=3)

x = polyglot.import_value(name="test")

print(x)

polyglot.eval(language="js", string='var x = Polyglot.import("test");\n console.log(x);')
