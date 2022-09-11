# RIOA
RIOA is a functional interpreted programming language created for Truttle1's practical language jam.
It's an acronym for (R)un (I)f (O)r (A)nd, which is the main feature of this language.

Documentation can be found here: https://dominicentek.github.io/RIOA
### Features and fixes to be added
- Array initialization
- Fix: Adding symbol at the very start of expression causes the symbol to be added as a part of operator
```
array = [1,2]
```
will result in a crash with "SyntaxError: Invalid operator ," because the interpreter thinks the operator is "=\[". Similar thing happens with modifiers after operators too.

***Features will be added after the jam judges finish judging this language.***
## Example programs
### Fibonacci number
```ansi
func fibonacci(number n) {
  if n < 2 {
    return n
  }
  else {
    return fibonacci(n - 2) + fibonacci(n - 1)
  }
}
func main(args) {
  print("Input number: ")
  println(fibonacci(parsenum(input())))
}
```
### Number guessing game
```ansi
func main(args) {
  println("I'm thinking of a number between 1 to 100. Try to guess it within 10 tries!")
  randomNumber = floor(random() * 100) + 1
  tries = 10
  while tries > 0 {
    print("Your input: ")
    number = parsenum(input())
    if randomNumber < number { println("Target is lower") }
    else if randomNumber > number { println("Target is higher") }
    else {
      println("You win!")
      exit(0)
    }
    tries--
  }
  println("You lose! Number: " + randomNumber)
}
```
### File search
```ansi
func filesearch(string filename, string word, boolean caseSensitive) {
  lines = split(bytestostring(ioread(filename)), "\n")
  i = 0
  while i < lines[] {
    line = lines[i]
    if (caseSensitive && includes(line, word)) || (!caseSensitive && includes(lowercase(line), lowercase(word))) {
      println(line)
    }
    i++
  }
}
func main(args) {
  if args[] < 2 {
    println("Usage:")
    println("<source file> <string to search for> [case sensitive (true|false) default: false]")
    exit(1)
  }
  casesensitive = false
  if args[] >= 3 {
    if args[2] == "true" { casesensitive = true }
    else if args[2] == "false" { casesensitive = false }
    else {
      println(args[2] + " is not a boolean")
      exit(1)
    }
  }
  filesearch(args[0], args[1], casesensitive)
}
```
