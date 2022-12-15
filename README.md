# Iota Interpreter

Writing an interpreter for the Turing tarpit language Iota designed by Chris Barker.

## Summary

This interpreter uses the syntax

```
t ::= i
    | * t t
```

where whitespaces are ignored. The Esolang page for [Iota](https://esolangs.org/wiki/Iota) uses the following computation rules:

```
*ix     --> **xsk
**kxy   --> x
***sxyz --> **xz*yz
```

As such, the interpreter makes use of the intermediate forms `S` and `K`, where their behavior is strictly defined as above.
In addition to the above evaluation rules, I added the following congruence rules:

```
     t1 --> t1'
--------------------
* t1 t2 --> * t1' t2

     t2 --> t2'
--------------------
* nf t2 --> * nf t2'
```

where "nf" represents a Normal Form. There are no defined values in this language.

## Example Code

Using string substitution, you can express the following concepts in Iota.

### SKI Combinators

```
I = *ii
K = *i*i*ii
S = *i*i*i*ii
```

### Church Booleans
```
tru x y = * * K x y
fls x y = * * * K I x y

and x y = * * * * S S * K * K fls x y
or x y = * * * * S I * K tru x y
not x = * * * S * * S I * K fls * K tru x
```
