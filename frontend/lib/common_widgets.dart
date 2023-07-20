import 'package:flutter/material.dart';

class ScrollableTable extends StatelessWidget {
  final TableRow? header;
  final List<TableRow> children;

  const ScrollableTable({super.key, this.header, this.children = const []});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        if (header != null) Table(children: [header!]),
        SingleChildScrollView(child: Table(children: children))
      ],
    );
  }
}

class NavBar extends StatefulWidget {
  final List<String> entries;
  final void Function(int)? onSelect;

  const NavBar({super.key, this.entries = const [], this.onSelect});

  @override
  State<StatefulWidget> createState() => NavBarState();
}

class NavBarState extends State<NavBar> {
  int? selected;

  void select(int index) {
    setState(() {
      selected = index;
    });
    if (widget.onSelect != null) {
      widget.onSelect!(index);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: widget.entries
          .asMap()
          .entries
          .map((entry) => Container(
                color: entry.key == selected
                    ? Theme.of(context).highlightColor
                    : null,
                child: TextButton(
                    onPressed: () => select(entry.key),
                    child: Text(entry.value)),
              ))
          .toList(),
    );
  }
}

class LoadingCircle extends StatelessWidget {
  const LoadingCircle({super.key});

  @override
  Widget build(Object context) {
    return Container(
      alignment: Alignment.center,
      child: const Text("Loading..."),
    );
  }
}
