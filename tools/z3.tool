<tool>
  <name>z3</name>
  <path>/usr/local/bin</path>
  <command>z3</command>
  <arguments>
    <argument>-st</argument>
  </arguments>
  <options>
  </options>
  <parser>
    <results>
      <result>
	<regex>^sat</regex>
	<value>SAT</value>
      </result>
      <result>
	<regex>unsat</regex>
	<value>UNSAT</value>
      </result>
    </results>
    <extras>
      <extra>
	<name>Restarts</name>
	<regex>:sat-restarts[ \\s]*([0-9]+)</regex>	
      </extra>
    </extras>
  </parser>
</tool>
