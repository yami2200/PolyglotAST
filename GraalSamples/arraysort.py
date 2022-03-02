
import polyglot


def complicated_number_computation():
    return 7

array = polyglot.import_value('tableau')
array[4] = complicated_number_computation()

print("Our array transferred to python :")
print(array)

array.sort() # uses the JS lexical order sorting, not python's list.sort() !

