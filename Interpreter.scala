/*
 * Iota is a Turing tarpit, meaning it is a Turing-complete language that aims to use the fewest number of linguistic elements.
 * Chris Barker designed the language using binary combinatory language. It reduces the SKI combinators to a single term.
 * 
 * i = λx.xsk
 *
 * From this, we can recover the SKI combinators: I = (ii), K = (i(i(ii))), S = (i(i(i(ii))))
 *
 * As such, this language is Turing complete.
 *
 * The interesting thing here is manipulating the concrete syntax to whichever two terms the user chooses.
 * For example, fans of the esolang CHICKEN may reduce the language to "chicken" and "\n".
 *
 * For this implementation, I will use the syntax defined on the Esolang page: t ::= "i" | "*" t t
 *
 * https://esolangs.org/wiki/Iota
 * https://en.wikipedia.org/wiki/Iota_and_Jot
 *
 * Author: Ronik Bhaskar
 */

import scala.io.Source

enum Token:
    case Star
    case I

enum Term:
    case Iota
    case S
    case K
    case App(t1: Term, t2: Term)

import Token.*
import Term.*

def nextToken(cs: List[Char]): Option[(Token,List[Char])] = cs match {
    case Nil => None
    case '*' :: tail => Some((Star, tail)) // application syntax
    case 'i' :: tail => Some((I, tail)) // iota combinator syntax
    case c :: tl if c.isWhitespace => nextToken(tl) 
    case c :: tail => throw new Exception(s"scan error: [$c] is an invalid symbol in the syntax")
}

def scan(code: String): List[Token] =
    def lp(cs:List[Char]): List[Token] = nextToken(cs) match {
        case None => Nil
        case Some(token, cs) => token :: lp(cs)
    }
    return lp(code.toList)

def nextTerm(toks: List[Token]): Option[(Term,List[Token])] = toks match {
    case Nil => None
    case I :: tail => Some((Iota, tail))
    case Star :: tail => nextTerm(tail) match {
        case None => throw new Exception(s"parse error: application (*) requires two terms after it")
        case Some(t1, tail) => nextTerm(tail) match {
            case None => throw new Exception(s"parse error: application (*) requires two terms after it")
            case Some(t2, tail) => Some((App(t1, t2), tail))
        }
    }
}

def parse(toks: List[Token]): Term = nextTerm(toks) match {
    case None => throw new Exception("not enough program")
    case Some(st,Nil) => st
    case Some(_,_) => throw new Exception("too much program")
}

def step(t: Term): Option[Term] = t match {
    /* computation rules */
    case App(Iota, t2) => Some(App(App(t2, S), K))
    case App(App(K, t2), t3) => Some(t2)
    case App(App(App(S, t2), t3), t4) => Some(App(App(t2, t4), App(t3, t4)))
    /* congruence rules */
    case App(t1, t2) => step(t1) match {
        case Some(t1_prime) => Some(App(t1_prime, t2))
        case None => step(t2) match {
            case Some(t2_prime) => Some(App(t1, t2_prime))
            case None => None
        }
    }
    case Iota => None
    case S => None
    case K => None
}

def steps(t: Term): List[Term] = t :: {step(t) match {
    case None => Nil
    case Some(t1) => steps(t1)
}}

def unparse(t: Term): String = t match {
    case Iota => "i "
    case S => "S "
    case K => "K "
    case App(t1, t2) => s"* ${unparse(t1)}${unparse(t2)}"
}

@main def interpret(codeOrFilename: String): Unit = {
  var code = codeOrFilename
  try {
    code = Source.fromFile(codeOrFilename).getLines.mkString
  } catch {
    case e: java.io.FileNotFoundException => "pass" // do nothing
  }
  println(code)
  println()
  try {
    val toks = scan(code)
    try {
        val term = parse(toks)
        try {
            val stps = steps(term)
            for s <- stps do {
                println(unparse(s))
            }
        } catch {
            case e => println(e)
        }
    } catch {
        case e => println(e)
    }
  } catch {
    case e => println(e)
  }
}