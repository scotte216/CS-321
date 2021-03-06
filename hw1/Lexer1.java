//----------------------------------------------------------------------
// A starter version of miniJava lexer (manual version). (For CS321 HW1)
//----------------------------------------------------------------------
//
// Scott Ewing
// Fall 2015
//
import java.io.*;

public class Lexer1 {
  private static FileReader input = null;
  private static int nextC = -1;   // buffer for holding next char	
  private static int line = 1;     // currect line position
  private static int column = 0;   // currect column position
  
  // Internal token code
  //
  enum TokenCode {
    // Tokens with multiple lexemes
    ID, INTLIT, DBLLIT, STRLIT,

    // Keywords
    //   "class", "extends", "static", "public", "main", "void", "boolean", 
    //   "int", "double", "String", "true", "false", "new", "this", "if", 
    //   "else", "while", "return", "System", "out", "println"
    CLASS, EXTENDS, STATIC, PUBLIC, MAIN, VOID, BOOLEAN, INT, DOUBLE, STRING, 
    TRUE, FALSE, NEW, THIS, IF, ELSE, WHILE, RETURN, SYSTEM, OUT, PRINTLN,

    // Operators and delimiters
    //   +, -, *, /, &&, ||, !, ==, !=, <, <=, >, >=, =, 
    //   ;, ,, ., (, ), [, ], {, }
    ADD, SUB, MUL, DIV, AND, OR, NOT, EQ, NE, LT, LE, GT, GE,  ASSGN,
    SEMI, COMMA, DOT, LPAREN, RPAREN, LBRAC, RBRAC, LCURLY, RCURLY;
  }

  // Token representation
  //
  static class Token {
    TokenCode code;
    String lexeme;
    int line;	   	// line # of token's first char
    int column;    	// column # of token's first char
    
    public Token(TokenCode code, String lexeme, int line, int column) {
      this.code=code; this.lexeme=lexeme;
      this.line=line; this.column=column; 
    }

    public String toString() {
      return String.format("(%d,%2d) %-10s %s", line, column, code, 
			   (code==TokenCode.STRLIT)? "\""+lexeme+"\"" : lexeme);
    }
  }

  static void init(FileReader in) throws Exception { 
    input = in; 
    nextC = input.read();
  }

  //--------------------------------------------------------------------
  // Do not modify the code listed above. Add your code below. 
  //

  private static class LexError extends Exception {
      public LexError(){
      }
      
      public LexError(String message)
      {
          super(message);
      }

      public LexError(Throwable cause)
      {
          super(cause);
      }
      public LexError(String message, Throwable cause){
          super(message,cause);
      }
  }

  // Utility routines
  //
  private static boolean isSpace(int c) {
    return (c == ' ' || c == '\t');
  }
  
  private static boolean isNewLine(int c){
    return ( c == '\n' || c == '\r');
  }

  private static boolean isLetter(int c) {
    return (('a' <= c && c <= 'z') || 'A' <= c && c <= 'Z');
  }

  private static boolean isDigit(int c) {
      return ('0' <= c && c <= '9');
  }

  private static boolean isOctal(int c) {
      return ('0' <= c && c <= '7');
  }

  private static boolean isHex(int c) {
      return (('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <=c && c <= 'H' ));
  }

  // Return next char
  //
  // - need to track both line and column numbers
  // 
  private static int nextChar() throws Exception {
    int c = nextC;
    nextC = input.read();
    if (c != -1)
    {
        column++;
    }

    return c;
  }

  // Return next token (the main lexer routine)
  //
  // - need to capture the line and column numbers of the first char 
  //   of each token
  //
  static Token nextToken() throws Exception {
    int c = nextChar();
    //skip whitespace
    while (isSpace(c) || isNewLine(c))
    {
        if (c == '\n')
        {
            column = 0;
            line++;
            //System.out.println("White space remover incremented line: "+ line);
        }
        c = nextChar();
    }
    //recognize comments and ignore: see ../lab2/lab2/05/Lexer1.java
    while (c == '/' && nextC =='/')
    {
        do {
            c = nextChar();//skip a character
        } while (!isNewLine(c) && c != -1);
        
        //New line could start with whitespace. Skip it!
        while (isSpace(c) || isNewLine(c))
        {
            if (c == '\n')
            {
                column = 0;
                line++;
            //System.out.println("White space remover in // block incremented line: "+ line);

            }
            //System.out.println("value of c/nextC: "+ Integer.toHexString(c) + "/" + Integer.toHexString(nextC) );
            c = nextChar();
        }

    }

    while (c == '/' && nextC == '*')
    {
        int firstCol = column;
        int firstLine = line;
        c = nextChar();//c is now at *
        do
        {
            if (c == '\n')
            {
                line++;
                column = 0;
            //System.out.println("White space remover in /* block incremented line: "+ line);
            }
            c = nextChar();//skip a character
        } while (c != -1 && (c != '*' || nextC != '/'));
        if (c == -1)
        {
            throw new LexError("at (" + firstLine + "," + firstCol + "). Unclosed block comments");
        }
        c = nextChar();//c is now /
        c = nextChar();// c is now the character after /
    }


    // reached <EOF>
    if (c == -1) 
        return null;
    //skip whitespace
    while (isSpace(c) || isNewLine(c))
    {
        if (c == '\n')
        {
            column = 0;
            line++;
            //System.out.println("White space remover after comment checks incremented line: "+ line);
        }
        c = nextChar();
    }
    // reached <EOF>
    if (c == -1)
        return null;

    //recognize ID and keywords

   if (isLetter(c))
    {
        int firstCol = column;
        StringBuilder buffer = new StringBuilder();
        buffer.append((char) c);
        while (isLetter(nextC) || isDigit(nextC))
        {
            c = nextChar();
            buffer.append((char) c);
        }
        String lex = buffer.toString();
        //Have our lexeme here. Determine if keyword or not.
        //See: ../lab2/lab2/04/Lexer1.java
        if (lex.equals("class"))
            return new Token(TokenCode.CLASS,lex,line, firstCol);
        if (lex.equals("extends"))
            return new Token(TokenCode.EXTENDS,lex,line,firstCol);
        if (lex.equals("static"))
            return new Token(TokenCode.STATIC,lex,line,firstCol);
        if (lex.equals("public"))
            return new Token(TokenCode.PUBLIC,lex,line,firstCol);
        if (lex.equals("main"))
            return new Token(TokenCode.MAIN,lex,line,firstCol);
        if (lex.equals("void"))
            return new Token(TokenCode.VOID,lex,line,firstCol);
        if (lex.equals("boolean"))
            return new Token(TokenCode.BOOLEAN,lex,line,firstCol);
        if (lex.equals("int"))
            return new Token(TokenCode.INT,lex,line,firstCol);
        if (lex.equals("double"))
            return new Token(TokenCode.DOUBLE,lex,line,firstCol);
        if (lex.equals("String"))
            return new Token(TokenCode.STRING,lex,line,firstCol);
        if (lex.equals("true"))
            return new Token(TokenCode.TRUE,lex,line,firstCol);
        if (lex.equals("false"))
            return new Token(TokenCode.FALSE,lex,line,firstCol);
        if (lex.equals("new"))
            return new Token(TokenCode.NEW,lex,line,firstCol);
        if (lex.equals("this"))
            return new Token(TokenCode.THIS,lex,line,firstCol);
        if (lex.equals("if"))
            return new Token(TokenCode.IF,lex,line,firstCol);
        if (lex.equals("else"))
            return new Token(TokenCode.ELSE,lex,line,firstCol);
        if (lex.equals("while"))
            return new Token(TokenCode.WHILE,lex,line,firstCol);
        if (lex.equals("return"))
            return new Token(TokenCode.RETURN,lex,line,firstCol);
        if (lex.equals("System"))
            return new Token(TokenCode.SYSTEM,lex,line,firstCol);
        if (lex.equals("out"))
            return new Token(TokenCode.OUT,lex,line,firstCol);
        if (lex.equals("println"))
            return new Token(TokenCode.PRINTLN,lex,line,firstCol);

       return new Token(TokenCode.ID,lex,line,firstCol);
    }

    //Recognize digit literals: integers, octals, hex, and double
    //See: ../lab2/lab2/03/Lexer1.java
   if (isDigit(c)){
        int firstCol = column;
        StringBuilder buffer = new StringBuilder();
        buffer.append((char) c);
        // if it starts with 0 we have 4 cases:
        // 1) it's just a 0
        // 2) it's octal, starting with 0
        // 3) it's a hex starting with 0
        // 4) it's a double starting with 0.
        // 5) Special case 0xxxx. (note dot) where x could be 0-9, will be double.

        if (c == '0')
        {
            //Hex
            if (nextC == 'x' || nextC == 'X')
            {
                do {
                    c = nextChar();
                    buffer.append((char) c);
                } while (isHex(nextC));
                
                String lex = buffer.toString();
                try {
                    Integer.decode(lex);
                    return new Token(TokenCode.INTLIT,lex,line,firstCol);
                } catch (Exception e) 
                {
                    throw new LexError("at (" + line + "," + firstCol + 
                                         "). Invalid hexadecimal literal: " + lex);
                }
            }
            // Special case. Consume until a dot is reached or if it's all digits. 
            else if (isDigit(nextC))
            {
                do {
                    c = nextChar();
                    buffer.append((char) c);
                } while (isDigit(nextC));
                //at this point we need to see if the next char is a '.' 
                if (nextC == '.')
                {
                    do {
                        c = nextChar();
                        buffer.append((char) c);
                    } while (isDigit(nextC));
                    String lex = buffer.toString();
                    try {
                        Double.parseDouble(lex);
                        return new Token(TokenCode.DBLLIT,lex,line,firstCol);
                    } catch (Exception e)
                    {
                        throw new LexError("at (" + line + "," + firstCol + 
                                             "). Invalid double literal: " + lex);
                    }
                }
                //if not, it's constructed like an octal but might be bad
                else
                {
                   String lex = buffer.toString();
                   try{
                         Integer.parseInt(lex,8);
                         return new Token(TokenCode.INTLIT,lex,line,firstCol);
                   } catch (Exception e)
                   {
                         throw new LexError("at (" + line + "," + firstCol +
                               "). Invalid octal literal: " +lex);
                   }
                }
            }
            //Octal
            else if (isOctal(nextC))
            {   
                do {
                    c = nextChar();
                    buffer.append((char) c);
                } while(isOctal(nextC));
                String lex = buffer.toString();

                try{
                    Integer.parseInt(lex,8);
                    return new Token(TokenCode.INTLIT,lex,line,firstCol);
                } catch (Exception e)
                {
                    throw new LexError("at (" + line + "," + firstCol + 
                                          "). Invalid octal literal: " + lex);
                }

            }
            else if (nextC == '.')
            {
                do{
                    c = nextChar();
                    buffer.append((char) c);
                } while (isDigit(nextC));

                String lex = buffer.toString();

                try {
                    Double.parseDouble(lex);
                    return new Token(TokenCode.DBLLIT,lex,line,firstCol);
                } catch (Exception e)
                {
                    throw new LexError("at (" + line + "," + firstCol + 
                                         "). Invalid double literal: " + lex);
                }
            }
            //singleton 0
            else 
            {
                String lex = buffer.toString();
                return new Token(TokenCode.INTLIT,lex,line,firstCol);
            }
        }

        while (isDigit(nextC))
        {
            c = nextChar();
            buffer.append((char) c);
        }

        if (nextC == '.'){
            do {
                c = nextChar();
                buffer.append((char) c);
            } while (isDigit(nextC));
        }
        
        String lex = buffer.toString();
        //is it an intlit or a dbllit
        
        if (lex.contains("."))
        {
            try{
                Double.parseDouble(lex);
                return new Token(TokenCode.DBLLIT,lex,line,firstCol);

            } catch (Exception e)
            {
                throw new LexError("at (" + line + "," + firstCol + 
                                     "). Invalid double literal: " + lex);
            }
        }
        else
        {
            try{
                Integer.parseInt(lex);
                return new Token(TokenCode.INTLIT,lex,line,firstCol);

            } catch (Exception e)
            {
                throw new LexError("at (" + line + "," + firstCol +
                                       "). Invalid decimal literal: " + lex);
            }
        }
   }
    // Special case: starts with . Could be double, could be delimiter
   if (c == '.'){
       int firstCol = column;
       StringBuilder buffer = new StringBuilder();
       buffer.append((char) c);
       String lex;
       if (isDigit(nextC))
       {
            do {
                c = nextChar();
                buffer.append((char) c);
            } while (isDigit(nextC));

            lex = buffer.toString();
            try{
                Double.parseDouble(lex);
                return new Token(TokenCode.DBLLIT,lex,line,firstCol);
            } catch (Exception e){
                throw new LexError("at (" + line + "," + firstCol + 
                                        "). Invalid double literal: " + lex);
            }
       }
       else
       {
           lex = buffer.toString();
           return new Token(TokenCode.DOT,lex,line,firstCol);
       }
   }

   //recognize string literals
   if (c == '\"'){
       int firstCol = column;
       StringBuilder buffer = new StringBuilder();
       while (!isNewLine(nextC) && nextC != '\"' )
       {
           c = nextChar();
           buffer.append((char) c);
       }

       String lex = buffer.toString();
       
       if (isNewLine(nextC))
       {
            throw new LexError("at (" + line + "," + firstCol + 
                      "). Ill-formed or unclosed string: \"" + lex);
       }
       c = nextChar();
       return new Token(TokenCode.STRLIT,lex,line,firstCol);
   }

   //Recognize operators and delimiters
 // see ../lab2/lab2/06/Lexer1.java
    int firstCol = column;
    switch(c)
    {
        case '+': return new Token(TokenCode.ADD,"+",line,firstCol);
        case '-': return new Token(TokenCode.SUB,"-",line,firstCol);
        case '*': return new Token(TokenCode.MUL,"*",line,firstCol);
        case '/': return new Token(TokenCode.DIV,"/",line,firstCol);
        case '&': {
            if (nextC == '&')
            {
                c = nextChar();
                return new Token(TokenCode.AND,"&&",line,firstCol);
            }
            break;
        }
        case '|': {
            if (nextC == '|')
            {
                c = nextChar();
                return new Token(TokenCode.OR,"||",line,firstCol);
            }
            break;
        }
        case '!': {
            if (nextC == '=')
            {
                c = nextChar();
                return new Token(TokenCode.NE,"!=",line,firstCol);
            }
            else
                return new Token(TokenCode.NOT,"!",line,firstCol);
        }
        case '=': {
            if (nextC == '=')
            {
                c = nextChar();
                return new Token(TokenCode.EQ,"==",line,firstCol);
            }
            else
                return new Token(TokenCode.ASSGN,"=",line,firstCol);
        }
        case '<': {
            if (nextC == '=')
            {
                c = nextChar();
                return new Token(TokenCode.LE,"<=",line,firstCol);
            }
            else
                return new Token(TokenCode.LT,"<",line,firstCol);
        }
        case '>': {
            if (nextC == '=')
            {
                c = nextChar();
                return new Token(TokenCode.GE,">=",line,firstCol);
            }
            else
                return new Token(TokenCode.GT,">",line,firstCol);
        }
        case ';': return new Token(TokenCode.SEMI,";",line,firstCol);
        case ',': return new Token(TokenCode.COMMA,",",line,firstCol);
        case '.': return new Token(TokenCode.DOT,".",line,firstCol);
        case '(': return new Token(TokenCode.LPAREN,"(",line,firstCol);
        case ')': return new Token(TokenCode.RPAREN,")",line,firstCol);
        case '[': return new Token(TokenCode.LBRAC,"[",line,firstCol);
        case ']': return new Token(TokenCode.RBRAC,"]",line,firstCol);
        case '{': return new Token(TokenCode.LCURLY,"{",line,firstCol);
        case '}': return new Token(TokenCode.RCURLY,"}",line,firstCol);

    }
    throw new LexError("at (" + line + "," + firstCol + "). Illegal character: " + (char)c);
   
  }

}
