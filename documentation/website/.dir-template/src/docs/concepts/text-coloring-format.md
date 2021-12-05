---
id: doc1
title: Nuts Text Format
sidebar_label: Nuts Text Format
---

${{include($"${resources}/header.md")}}

# Nuts Text Format
**```nuts```** comes up with a simple coloring syntax that helps writing better looking portable command line programs.
standard output is automatically configured to accept the "Nuts Text Format" (NTF) syntax. 
Though it remains possible to disable this ability using the --no-color standard option (or programmatically, 
see **```nuts```** API documentation). NTF will be translated to the underlying terminal implementation using ANSI 
escape code on linux/windows terminals if available.

Here after a showcase of available NTF syntax.

![text-coloring-format](text-coloring-format-01.png)


![text-coloring-format](text-coloring-format-02.png)


![text-coloring-format](text-coloring-format-03.png)


![text-coloring-format](text-coloring-format-04.png)
