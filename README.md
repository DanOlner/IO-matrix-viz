IO-matrix-viz
=============

Interactive force-directed graph visualisation of an input-output matrix (From the UK ['combined use' matrix 2012]( http://www.ons.gov.uk/ons/publications/re-reference-tables.html?edition=tcm%3A77-379304)). 

Download/unzip dist.zip, double-click the jar, read the readme for instructions.

Source code uses these libraries: core.jar from [processing.org](https://processing.org/download/?processing) // [Jung 2 with included 3rd party libraries](http://jung.sourceforge.net/download.html) // [OpenCSV 2.2](http://sourceforge.net/projects/opencsv/files%2Fopencsv%2F2.2/). (Dist.zip's lib folder has all the required jars too.)

If you want to drop in your own matrix, have a look at the CSV format in dist's data folder. There are no headers. First column lists the names of rows/columns (industrial sectors, in the case of the one supplied). Only matrix cell values are in the CSV: the program sums rows and columns to get, respectively, per-sector demand and consumption.

Example use: on opening, force-based layout looks like:

<img src="http://danolner.github.io/IO-matrix-viz/images/MonNov2418_18_34GMT2014_KeyPrint_IOMatrix-0173.jpeg" width="400"/>

Set to circle:

<img src="https://github.com/DanOlner/IO-matrix-viz/blob/gh-pages/images/MonNov2418_18_40GMT2014_KeyPrint_IOMatrix-0277.jpeg" width="400"/>

Hover over the biggest single sector, construction:

<img src="https://github.com/DanOlner/IO-matrix-viz/blob/gh-pages/images/MonNov2418_18_45GMT2014_KeyPrint_IOMatrix-0537.jpeg" width="400"/>

Click on construction - keeps the top 5% of incoming/outgoing flows:

<img src="https://github.com/DanOlner/IO-matrix-viz/blob/gh-pages/images/MonNov2418_18_52GMT2014_KeyPrint_IOMatrix-0953.jpeg" width="400"/>

Move it:

<img src="https://github.com/DanOlner/IO-matrix-viz/blob/gh-pages/images/MonNov2418_18_55GMT2014_KeyPrint_IOMatrix-1146.jpeg" width="400"/>

Click on a couple of others who spend a lot on construction:

<img src="https://github.com/DanOlner/IO-matrix-viz/blob/gh-pages/images/MonNov2418_19_24GMT2014_KeyPrint_IOMatrix-2855.jpeg" width="400"/>

Add the money amounts (millions):

<img src="https://github.com/DanOlner/IO-matrix-viz/blob/gh-pages/images/MonNov2418_37_16GMT2014_KeyPrint_IOMatrix-3117.jpeg" width="400"/>
