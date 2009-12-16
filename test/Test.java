/*
 * Test.java
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

import edu.rit.poe.atomix.levels.Connector;
import edu.rit.poe.atomix.levels.Level;
import edu.rit.poe.atomix.levels.Atom;
import java.io.FileInputStream;
import junit.framework.TestCase;

/**
 *
 * @author  Peter O. Erickson
 *
 * @version $Id$
 */
public class Test extends TestCase {
    
    public void testLoad() throws Exception {
        
        FileInputStream is = new FileInputStream( "level1.level" );
        
        Level level1 = Level.loadLevel( is );
        
        for ( Atom molecule : level1.getMolecules() ) {
            System.out.println( "Molecule: " + molecule.getElement() );
            System.out.println( "Connectors:" );
            for ( Connector connector : molecule.getConnectors() ) {
                System.out.println( " Direction: " + connector.getDirection() );
                System.out.println( " Bond: " + connector.getBond() );
            }
        }
    }
    
} // Test
