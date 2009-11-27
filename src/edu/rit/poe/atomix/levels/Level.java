/*
 * Level.java
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without major
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, and/or distribute copies of, but
 * under no circumstances sublicense and/or sell, the Software,
 * and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.rit.poe.atomix.levels;

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Level implements Serializable {
    
    /**
     * Enumerated type of sections of a level file.
     * 
     * @author  Peter O. Erickson
     * 
     * @version $Id$
     */
    private static enum LevelFileSection {
        
        LEVEL,
        
        NAME,
        
        FORMULA,
        
        SIZE,
        
        MOLECULES,
        
        MAP,
        
        GOAL_SIZE,
        
        GOAL;
        
        static LevelFileSection parse( String str ) {
            LevelFileSection section = null;
            
            for ( LevelFileSection sect :
                    EnumSet.allOf( LevelFileSection.class ) ) {
                if ( str.equalsIgnoreCase( ( sect.name() ).toLowerCase() ) ) {
                    section = sect;
                }
            }
            
            return section;
        }
        
    } // LevelFileSection
    
    private int level;
    
    private String name;
    
    private String formula;
    
    private Square[][] board;
    
    private Square[][] goal;
    
    private Set<Atom> molecules;
    
    /**
     * Constructs a new <tt>Level</tt>.
     */
    private Level() {
    }
    
    public int getLevel() {
        return level;
    }
    
    public Set<Atom> getMolecules() {
        return molecules;
    }
    
    public Square[][] copyBoard() {
        // make a full copy of the baord
        Square[][] copy = new Square[ board.length ][ board[ 0 ].length ];
        for ( int i = 0; i < board.length; i++ ) {
            System.arraycopy( board[ i ], 0, copy[ i ], 0, board[ i ].length );
        }
        
        return copy;
    }
    
    /**
     * This method is used to check for success conditions on a board.
     * 
     * @param   board   the board to check for completeness
     * 
     * @return          <tt>true</tt> if the board is complete, otherwise
     *                  <tt>false</tt>
     */
    public boolean isComplete( Square[][] board ) {
        boolean retVal = false;
        
        int ymax = board.length - goal.length;
        int xmax = board[ 0 ].length - goal[ 0 ].length;
        
        // pass a kernel over the board.  not the most efficient, but it works
        for ( int y = 0; ( ( y < ymax ) && ( ! retVal ) );  y++ ) {
            for ( int x = 0; ( ( x < xmax ) && ( ! retVal ) ); x++ ) {
                boolean goalFound = true;
                
                // check all parts of the kernel
                for ( int y0 = 0; ( ( y0 < goal.length ) && goalFound );
                        y0++ ) {
                    for ( int x0 = 0; ( ( x0 < goal[ 0 ].length )
                            && goalFound ); x0++ ) {
                        
                        Square boardSqr = board[ y + y0 ][ x + x0 ];
                        Square goalSqr = goal[ y0 ][ x0 ];
                        
                        if ( ( ! ( goalSqr instanceof Square.Empty ) ) &&
                                ( boardSqr != goalSqr ) ) {
                            goalFound = false;
                        }
                    }
                }
                retVal = goalFound;
            }
        }
        return retVal;
    }
    
    public static final Level loadLevel( InputStream is )
            throws FileNotFoundException {
        Level level = new Level();
        level.molecules = new HashSet<Atom>();
        
        BufferedReader in = new BufferedReader( new InputStreamReader( is ) );
        
        String line = null;
        LevelFileSection section = null;
        Map<Short, Atom> atomMap = new HashMap<Short, Atom>();
        int y = -1;
        int goalY = -1;
        try {
            while( ( line = in.readLine() ) != null ) {
                
                LevelFileSection s = null;
                
                if ( line.startsWith( "#" ) ) {
                    // comment line, ignore
                } else if ( line.trim().equals( "" ) ) {
                    // empty line, ignore
                } else if ( ( s = LevelFileSection.parse(
                        line.replaceAll( ":", "" ) ) ) != null ) {
                    section = s;
                } else {
                    switch ( section ) {
                        case LEVEL: {
                            // parse the level number
                            try {
                                int levelNum = Integer.parseInt( line.trim() );
                                
                                level.level = levelNum;
                            } catch ( Exception e ) {
                                Log.e( "LevelLoader",
                                        "Error setting the level number." );
                            }
                        } break;
                        
                        case NAME: {
                            level.name = line;
                        } break;
                        
                        case FORMULA: {
                            level.formula = line;
                        } break;
                        
                        case SIZE: {
                            // parse the size of the board
                            try {
                                String[] args = line.split( "[Xx]" );
                                
                                int width = Integer.parseInt( args[ 0 ] );
                                int height = Integer.parseInt( args[ 1 ] );
                                
                                level.board = new Square[ height ][ width ];
                            } catch ( Exception e ) {
                                // error creating level map!
                                //@todo handle this exception
                            }
                        } break;
                        
                        case MOLECULES: {
                            try {
                                String[] args = line.split( "\\s+" );
                                
                                short id = Short.parseShort( args[ 0 ] );
                                String color = args[ 1 ];
                                char element = args[ 2 ].charAt( 0 );
                                
                                Set<Connector> connectors =
                                        new HashSet<Connector>();
                                for ( int i = 3; i < args.length; i++ ) {
                                    // bond type -/=
                                    Connector.Bond bond = null;
                                    char b = args[ i ].charAt( 0 );
                                    if ( b == '-' ) {
                                        bond = Connector.Bond.SINGLE;
                                    } else if ( b == '=' ) {
                                        bond = Connector.Bond.DOUBLE;
                                    }
                                    
                                    // the connector direction
                                    Connector.Direction direction =
                                            Connector.Direction.parse(
                                            args[ i ].substring( 1 ) );
                                    
                                    // add new connector to set
                                    connectors.add(
                                            new Connector( direction, bond ) );
                                }
                                
                                Atom molecule = new Atom( id, color, element,
                                        connectors );
                                
                                ( level.molecules ).add( molecule );
                                Log.d( "LevelLoader",
                                        "Setting molecule " + id );
                                atomMap.put( id, molecule );
                            } catch ( Exception e ) {
                                //@todo handle this exception
                            }
                        } break;
                        
                        case MAP: {
                            char[] row = line.toCharArray();
                            
                            y++;
                            for ( int x = 0; x < row.length; x++ ) {
                                Square sqr = null;
                                
                                if ( row[ x ] == 'X' ) {
                                    sqr = Square.WALL;
                                } else if ( row[ x ] == ' ' ) {
                                    sqr = Square.EMPTY;
                                } else if ( row[ x ] == 'B' ) {
                                    sqr = null;
                                } else if ( Character.isDigit( row[ x ] ) ) {
                                    short id = -1;
                                    try {
                                        id = Short.parseShort( "" + row[ x ] );
                                        Atom atom = atomMap.get( id );
                                        
                                        // assign the X, Y of the Atom
                                        atom.setX( x );
                                        atom.setY( y );
                                        
                                        sqr = atom;
                                    } catch ( Exception e ) {
                                        Log.e( "LevelLoader",
                                                Log.getStackTraceString( e ) );
                                    }
                                }
                                
                                level.board[ y ][ x ] = sqr;
                            }
                            
                        } break;
                        
                        case GOAL_SIZE: {
                            // parse the size of the board
                            try {
                                String[] args = line.split( "[Xx]" );
                                
                                int width = Integer.parseInt( args[ 0 ] );
                                int height = Integer.parseInt( args[ 1 ] );
                                
                                level.goal = new Square[ height ][ width ];
                            } catch ( Exception e ) {
                                Log.e( "LevelLoader",
                                        "Error setting new goal array" );
                            }
                        } break;
                        
                        case GOAL: {
                            Log.d( "LevelLoader", "Loading goal..." );
                            char[] row = line.toCharArray();
                            Log.d( "LevelLoader", "Goal length: "
                                    + row.length );
                            
                            goalY++;
                            for ( int x = 0; x < row.length; x++ ) {
                                Square sqr = null;
                                
                                if ( Character.isDigit( row[ x ] ) ) {
                                    short id = -1;
                                    try {
                                        id = Short.parseShort( "" + row[ x ] );
                                        sqr = atomMap.get( id );
                                    } catch ( Exception e ) {
                                        Log.e( "LevelLoader",
                                                Log.getStackTraceString( e ) );
                                    }
                                } else if ( row[ x ] == ' ' ) {
                                    sqr = Square.EMPTY;
                                }
                                
                                level.goal[ goalY ][ x ] = sqr;
                            }
                        } break;
                    }
                }
            }
        } catch ( Exception e ) {
            // ignore for now
        }
        
        return level;
    }
    
} // Level
