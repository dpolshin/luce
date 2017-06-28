 Simple Indexing and Search tool.
=================================


Application provides language-agnostic (1) indexing and search through UTF-8 encoded text files.

Features:
- search through through single or multiple words
- two match modes for multiple word query: either all words should present in file, or search should match exact query (2).
- case insensitive
- preview for search result with highlights and navigation
- live-update index when files are changed (3)
- stop-words filtering for English and Russian languages
- persistent index storage, not requires scan after restart


---------------------------------
(1) except of stop words filtering
(2) exact match not taking into account punctuation, whitespaces and case
(3) only when application is running
