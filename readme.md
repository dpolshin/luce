 Simple Indexing and Search tool.
=================================


Application provides language-agnostic indexing and search through UTF-8 encoded text files.

Features:
- search through through single or multiple words
- two match modes for multiple word query: either all words should present in file, or search should match exact query
- case insensitive
- preview for search result with highlights and navigation
- live-update index when files are changed when application is running
- persistent index storage, not requires scan after restart
- non-blocking search action
- non-blocking index action
- can index large repositories (60k files in 3min)
- can index large files (3GB files in 10min)


---------------------------------

Known Issues and Shortcuts:
- if indexed file is to large to fit into one index segment and search found 
    occurrences in several segments, they will be presented several times in result
- search result length is limited to 5k items, 
    however, can be easily extended in future if it makes sense
- file length is limited to java.lang.Integer.MAX_VALUE characters, so around 3GB is ok
- index serializer uses kryo library, however, custom binary serialization protocol has been developed
    and is on par with kryo or slightly faster and provides more functionality, like partial read (header only) and streaming
- maximum file size for preview limited to 10MB, for larger files offset positions of found matches are shown
- application does not rescan files and directories for changes upon start
- application does not track changes for single files added to application, only directories are kept track on


