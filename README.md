# large-file-reading-challenge
<p>
The application diagram describing <a href="https://github.com/Ee-Cs/large-file-reading-challenge/blob/main/docs/mermaid/stateDiagram.md">the using of the MappedByteBuffer</a>.
</p>
<hr>
<p>The 3GB data file <i>city_temperatures.csv</i> content</p>
<p>
The application is using the 
<a href="https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/nio/MappedByteBuffer.html">MappedByteBuffer</a> 
 - a direct byte buffer whose content is a memory-mapped region of a file.
The CSV file is read and the computed average temperatures are kept in Map object.
For every next request to the endpoint CSV data file modification time is checked.
The file is reread if it was externally changed. 
</p>
<ul>
<li>file format - comma separated values</li>
<li>one hundred biggest US cities (
<a href="https://github.com/Ee-Cs/large-file-reading-challenge/blob/main/src/main/resources/static/index.html#L104">
combo</a> )</li>
<li>a half-century of samples<br>
    (the temperature was sampled every half an hour)
    <ul>
        <li>first date is 1970-01-01 01:00:00 (one o'clock at night)</li>
        <li>last  date is 2019-12-31 23:30:00</li>
    </ul>
    </li>
<li>the used temperatures were between -50 and 50 degrees</li>
</ul>
<hr/>
<p>
For the data filled with random temperatures
expected yearly average should be near zero.  
<img alt="" src="docs/images/WebPageScreenshot.png"/>
</p>
<hr>
<p>
The controller method:
<a href="https://github.com/Ee-Cs/large-file-reading-challenge/blob/main/src/main/java/kp/controllers/KpController.java#L44">
kp.controllers.KpController::getAverageTemperatures</a>.
</p>
<p>
The file reading method:
<a href="https://github.com/Ee-Cs/large-file-reading-challenge/blob/main/src/main/java/kp/services/KpService.java#L92">
kp.services.KpService::readFile</a>.
</p>
<p>
The text line matching method:
<a href="https://github.com/Ee-Cs/large-file-reading-challenge/blob/main/src/main/java/kp/services/KpService.java#L171">
kp.services.KpService::readMatchedLine</a>.
</p>
<p>
The averages map serves as the cache for the big file reading results.<br>
The averages map reading method:
<a href="https://github.com/Ee-Cs/large-file-reading-challenge/blob/main/src/main/java/kp/services/KpService.java#L71">
kp.services.KpService::getAveragesList</a>.
</p>
<p>
The frontend logic is in the web page:
<a href="https://github.com/Ee-Cs/large-file-reading-challenge/blob/main/src/main/resources/static/index.html">
index.html</a>.
</p>
<p>
The <a href="https://github.com/Ee-Cs/large-file-reading-challenge/tree/main/src/test/java/kp">
tests</a>.<br>
The controller tests use JUnit Jupiter and MockMvc.
</p>
<hr>
