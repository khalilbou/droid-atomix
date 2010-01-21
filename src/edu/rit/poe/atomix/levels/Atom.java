/*
 * Atom.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an individual atom.  This class stores the current
 * location, ID, atom character and color of an atom.
 * 
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public final class Atom extends Square {
    
    /** A map of atom element letter to hexadecimal color. */
    private static final Map<Character, String> colorMap;
    
    // Setup the color map defaults
    static {
        colorMap = new HashMap<Character, String>();
        
        // set the default atom colors
        colorMap.put( 'H', "0000FF" );
        colorMap.put( 'O', "FF0000" );
        colorMap.put( 'C', "444444" );
    }
    
    /** The short ID of this atom. */
    private short id;
    
    /** The atom character. */
    private char element;
    
    /** The connectors associated with this atom. */
    private Set<Connector> connectors;
    
    /**
     * Constructs a new <tt>Atom</tt>.
     * 
     * @param   id          the atom ID
     * @param   color       the color of the atom
     * @param   element     the element letter
     * @param   connectors  the atom connectors
     */
    public Atom( short id, char element, Set<Connector> connectors ) {
        this.id = id;
        this.element = element;
        this.connectors = connectors;
    }

    public short getId() {
        return id;
    }
    
    public String getColor() {
        return colorMap.get( element );
    }
    
    public char getElement() {
        return element;
    }
    
    public Set<Connector> getConnectors() {
        return connectors;
    }
    
    /**
     * Compares this <tt>Atom</tt> to the specified object for equality.
     * 
     * @param   o   
     * 
     * @return
     */
    @Override
    public boolean equals( Object o ) {
        boolean retVal = false;
        if ( o instanceof Atom ) {
            Atom a = ( Atom )o;
            
            retVal = ( ( element == a.element ) &&
                    ( connectors.containsAll( a.connectors ) ) );
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return id;
    }
    
    /**
     * Returns this atom in <tt>String</tt> form.
     * 
     * @return  this atom as a <tt>String</tt>
     */
    @Override
    public String toString() {
        return Character.toString( element );
    }
    
} // Atom
