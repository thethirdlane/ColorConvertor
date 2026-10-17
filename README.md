# Color Convertor

This is a very (very) simple program for converting colors from
RGB to HEX to HSL and back.  No fancy color pickers or color wheels,
just basic cut and paste.  It came to be because none of the available
color pickers on ubuntu were working for me.

The program is written in Kotlin and requires a JVM to run.

## Compiling

Maven is the build system.  May need to be installed.

This program depends on `https://github.com/thethirdlane/SwingDSL.git`
SwingDSL is a kotlin Swing DSL library written by moi that is
used to create the Gui.  That project is not published to a public Maven repository
(neither is this one) and so needs to be cloned and
built locally before ColorConvertor can be built.  See the README in that project
for more details.

Once SwingDSL is available in the local Maven repository you can build
ColorConvertor with:

`mvn -DskipTests package`

Alternatively, if you are familiar with the trials and tribulations of building Graal
Native Images, the pom file has the set up to create a native image.
This requires a graal capable VM, like *Liberica*.  The VM I
am using currently is *Liberica-NIK-25.0.1-1*

To compile the native image:

`mvn -DskipTests -Pnative package`

If all goes well, you should get an executable called *colorconvertornative*. If not, 
welcome to Graal.  You may have to do some research.  I *can* promise you that it 
is possible to do.

## Running
- With a VM
 
  `java -jar target/ColorConvertor-1.0-jar-with-dependencies.jar`
 
  or, if you prefer the longer route:
 
  `java -cp target/ColorConvertor-1.0-jar-with-dependencies.jar org.ttl.color.ColorConvertorApp`

- If you have built the native executable, simple run it:
  `./colorconvertornative`

- Important - When running from IntelliJ on linux, set this property:
  - `-Dsun.awt.disablegrab=true`
    Else IntelliJ freezes the whole desktop sometimes when debugging, e.g.
    with JComboBoxes.  This may or may not apply to your particular distribution.
   
## Using
- Type something in one format and hit enter.  It is converted into the other
formats.
- Foreground and Background affect the Display Panel.
- `C` and `P` do Copy/Paste
- ComboBox in the north changes Look And Feel
- `Suggest` buttons offer some suggestions for colors for the
"other side" of the panel, i.e Foreground suggests Background
colors and vice versa.  It runs a bunch of algorithms which i have
copied and pasted from Gemini.  I have no idea how good the algorithms
are, and have noticed that they tend to loose the plot when you you have
rgb numbers higher than around 160 - the colors suggested suddenly become
too dark.
- Pro tip - if you are playing around with HSL values, it may be a good idea
to keep all the values with 2 decimals, e.g. 120.00 for hue, 0.72 for 
saturation etc.  That keeps the cursor in the combobox textfield in place.
If you don't have the 2 decimals, the cursor moves to the end on update.
And you wind up cursing the cursor.  And the developer.

## Implementation Notes 
- Written in Kotlin
- Google Gemini has had a hand in creating some bits and pieces.

## License
MIT License

Enjoy.