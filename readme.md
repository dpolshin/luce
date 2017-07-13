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


---------------------------------

Known Issues:
- not possible to cancel running task, either search or indexing
- if indexed file is to large to fit into one index segment and search found 
    occurrences in several segments, they will be presented several times in result
- search result length is limited to 5k items, 
    however, can be easily extended in future if it makes sense.
