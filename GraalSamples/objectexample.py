
import polyglot

class TestSubject:
    
    def __init__(self, age=42, name="John", scientist=True):
        self.age = age
        self.name = name
        self.scientist = scientist
        
    def foo(self):
        return len(self.name)
    
    def __str__(self):
        if self.scientist:
            return self.name + f" is a {self.age} years old scientist"
        return self.name + f" is a {self.age} years old test subject"
    
test = TestSubject()

print("Displaying John in python : ")
print(test)

polyglot.export_value(value=test, name='John')