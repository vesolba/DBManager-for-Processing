/**
 * you can put a one sentence description of your tool here.
 *
 * (c) 2017
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * @author   Venerando Solis Barrado https://github.com/vesolba
 * @modified 06/26/2017
 * @version  1.0.0
 */

package template.tool;

import processing.app.Base;
import processing.app.tools.Tool;
import processing.app.ui.Editor;


// when creating a tool, the name of the main class which implements Tool must
// be the same as the value defined for project.name in your build.properties

public class HelloTool implements Tool {
  Base base;


  public String getMenuTitle() {
    return "DB Manager for Processing";
  }


  public void init(Base base) {
    // Store a reference to the Processing application itself
    this.base = base;
  }


  public void run() {
    // Get the currently active Editor to run the Tool on it
    Editor editor = base.getActiveEditor();

    // Fill in author.name, author.url, tool.prettyVersion and
    // project.prettyName in build.properties for them to be auto-replaced here.
    System.out.println("Hello Tool. DB Manager for Processing 1.0.0 by Venerando Solis Barrado https://github.com/vesolba");
  }
}
