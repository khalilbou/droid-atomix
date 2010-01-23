/*
 * Level.java
 * 
 * Version:
 *      $Id$
 *
 * Copyright (c) 2009 Peter O. Erickson
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package edu.rit.poe.atomix.levels;

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class to represent a level, by containing information such as the layout of
 * the board, the atoms on the board, and the solution.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Level implements Comparable<Level> {
    
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
    
    private Map<Short, Atom> atoms;
    
    /**
     * Constructs a new <tt>Level</tt>.
     */
    private Level() {
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFormula() {
        return formula;
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
     * Returns the level's board.  This should be treated as immutable -- never
     * change this!
     * 
     * @return  the level's underlying board
     */
    public Square[][] getBoard() {
        return board;
    }
    
    /**
     * Retrieves an <tt>Atom</tt> by its ID.
     * 
     * @param   id  the <tt>short</tt> ID of the atom to be returned
     * 
     * @return      the atom associated with the specified ID
     */
    public Atom getAtom( Short id ) {
        return atoms.get( id );
    }
    
    /**
     * Returns this level's goal configuration.  The returned array should
     * <b>not</b> be modified in any way.
     * 
     * @return  the goal configuration
     */
    public Square[][] getGoal() {
        return goal;
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
                        
                        // @todo fix this mess!!  now hiring: better algorithm
                        // throwing ideas against a wall: Square.NULL instead of
                        // using null and use .equals() for all comparison?
                        // could work........
                        if ( ( boardSqr instanceof Atom ) &&
                                ( goalSqr instanceof Atom ) ) {
                            Atom boardAtom = ( Atom )boardSqr;
                            Atom goalAtom = ( Atom )goalSqr;
                            // the .equals() comparison is deep, and checks more
                            // than just ID, to allow duplicate atoms to be
                            // interchangeable
                            if ( ! goalAtom.equals( boardAtom ) ) {
                                goalFound = false;
                            }
                        } else if ( ( goalSqr instanceof Atom ) &&
                                ( ! ( boardSqr instanceof Atom ) ) ) {
                            goalFound = false;
                        } else if ( ( boardSqr instanceof Atom ) &&
                                ( ! ( goalSqr instanceof Atom ) ) ) {
                            goalFound = false;
                        }
                    }
                }
                retVal = goalFound;
            }
        }
        return retVal;
    }
    
    /**
     * Compares this level to the specified one for sorting purposes.
     * 
     * @param   other   the level to compare to this one
     * 
     * @return          a negative integer, zero, or a positive integer as this
     *                  level is less than, equal to, or greater than the
     *                  specified level
     */
    public int compareTo( Level other ) {
        return ( level > other.level ? 1 : ( level < other.level ? -1 : 0 ) );
    }
    
    public static final Level loadLevel( InputStream is )
            throws FileNotFoundException, LevelFileFormatException {
        Level level = new Level();
        BufferedReader in = new BufferedReader( new InputStreamReader( is ) );
        
        String line = null;
        LevelFileSection section = null;
        level.atoms = new HashMap<Short, Atom>();
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
                                throw new LevelFileFormatException(
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
                                throw new LevelFileFormatException(
                                        "Invalid board size." );
                            }
                        } break;
                        
                        case MOLECULES: {
                            try {
                                String[] args = line.split( "\\s+" );
                                
                                short id = 0;
                                try {
                                    id = Short.parseShort( args[ 0 ] );
                                } catch ( NumberFormatException e ) {
                                    Log.d( "LEVEL", "PARSE CHARACTER" );
                                    id = ( short )( 10
                                            + ( args[ 0 ].charAt( 0 ) - 'a' ) );
                                }
                                char element = args[ 1 ].charAt( 0 );
                                
                                Set<Connector> connectors =
                                        new HashSet<Connector>();
                                for ( int i = 2; i < args.length; i++ ) {
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
                                
                                Atom molecule = new Atom( id, element,
                                        connectors );
                                
                                level.atoms.put( id, molecule );
                            } catch ( Exception e ) {
                                throw new LevelFileFormatException(
                                        "Error with atom formats." );
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
                                } else {
                                    short id = -1;
                                    try {
                                        try {
                                            id = Short.parseShort(
                                                    Character.toString(
                                                    row[ x ] ) );
                                        } catch ( NumberFormatException e ) {
                                            id = ( short )( 10
                                                    + ( row[ x ] - 'a' ) );
                                        }
                                        
                                        sqr = level.getAtom( id );
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
                                throw new LevelFileFormatException(
                                        "Error setting new goal array" );
                            }
                        } break;
                        
                        case GOAL: {
                            char[] row = line.toCharArray();
                            
                            goalY++;
                            for ( int x = 0; x < ( level.goal[ goalY ].length );
                                    x++ ) {
                                Square sqr = null;
                                if ( ( x >= row.length ) ||
                                        ( row[ x ] == ' ' ) ) {
                                    sqr = Square.EMPTY;
                                } else {
                                    short id = -1;
                                    try {
                                        try {
                                            id = Short.parseShort(
                                                    Character.toString(
                                                    row[ x ] ) );
                                        } catch ( NumberFormatException e ) {
                                            id = ( short )( 10
                                                    + ( row[ x ] - 'a' ) );
                                        }
                                        
                                        sqr = level.getAtom( id );
                                    } catch ( Exception e ) {
                                        Log.e( "LevelLoader",
                                                Log.getStackTraceString( e ) );
                                    }
                                }
                                
                                level.goal[ goalY ][ x ] = sqr;
                            }
                        } break;
                    }
                }
            }
        } catch ( Exception e ) {
            throw new LevelFileFormatException( "Error reading level file:"
                    + Log.getStackTraceString( e ) );
        }
        
        return level;
    }
    
} // Level
