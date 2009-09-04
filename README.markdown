Overview:
--------

This is my second submission to the GitHub contest.  My initial approach was in Ruby and I found it too slow to be usable.  This project
is mostly a port to Java and performs considerably better.  Since it is a port and I was short of time, the variable naming convention
often uses underscores rather than camel-casing.  The code is fairly clean, but not the best structured Java application by any stretch of the
imagination.

For more details on the algorithm used, please ready my [write-up](http://nirvdrum.com/2009/09/03/github-contest-recap.html).

Running:
-------

The project is structured using maven.  You must have maven 2.x on your path; I used 2.2.1 throughout the development of the application.

To run the tests:

<code>
$ mvn test
</code>

To run the project in evaluation mode (runs in 10 fold cross validation):

<code>
$ mvn package
$ java -Xmx2048m -server -jar target/github_contest-1.0.jar training
</code>

To run the project and generate the results.txt file in the current working directory:

<code>
$ mvn package
$ java -Xmx2048m -server -jar target/github-contest-1.0.jar
</code>


License:
-------

The code is licensed under the Apache Software License v2.  Please see the LICENSE file for the full license text.