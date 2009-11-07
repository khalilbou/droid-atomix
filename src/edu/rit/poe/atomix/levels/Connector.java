/*
 * Connector.java
 *
 * Version:
 *      $Id$
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

import java.io.Serializable;
import java.util.EnumSet;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Connector implements Serializable {
    
    public static enum Direction {
        
        UP( "u" ),
        
        DOWN( "d" ),
        
        RIGHT( "r" ),
        
        LEFT( "l" ),
        
        UPPER_RIGHT( "ur" ),
        
        UPPER_LEFT( "ul" ),
        
        LOWER_RIGHT( "lr" ),
        
        LOWER_LEFT( "ll" );
        
        private String name;
        
        Direction( String name ) {
            this.name = name;
        }
        
        static Direction parse( String str ) {
            Direction direction = null;
            
            for ( Direction dir : EnumSet.allOf( Direction.class ) ) {
                if ( str.equalsIgnoreCase( dir.name ) ) {
                    direction = dir;
                }
            }
            
            return direction;
        }
        
    } // Direction
    
    public static enum Bond {
        
        SINGLE,
        
        DOUBLE;
        
    } // Bond
    
    private Direction direction;
    
    private Bond bond;
    
    /**
     * Constructs a new <tt>Connector</tt>.
     * @param direction
     * @param bond
     */
    public Connector( Direction direction, Bond bond ) {
        this.direction = direction;
        this.bond = bond;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public Bond getBond() {
        return bond;
    }
    
} // Connector
