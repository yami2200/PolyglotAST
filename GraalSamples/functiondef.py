import polyglot

def foo(x, y):
    return x**2 + y**2


polyglot.export_value(value=foo, name='foo')

