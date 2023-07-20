import 'package:flutter/material.dart';

import 'common_widgets.dart';
import 'model.dart';
import 'server.dart';

class MarketView extends StatefulWidget {
  final GameServer server;

  const MarketView({required this.server, super.key});

  @override
  State<StatefulWidget> createState() => MarketViewState();
}

class MarketViewState extends State<MarketView> {
  bool showInventory = true;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            Column(
              children: [
                Container(),
                NavBar(),
                TextButton(
                    onPressed: () => setState(() {
                          showInventory = true;
                        }),
                    child: const Row(
                      children: [Text("Inventory")],
                    ))
              ],
            ),
            IndexedStack(
              index: showInventory ? 0 : 1,
              children: [Placeholder(), OrderView()],
            )
          ],
        ),
      ],
    );
  }
}

class OrderView extends StatefulWidget {
  const OrderView({super.key});

  @override
  State<StatefulWidget> createState() => OrderViewState();
}

class OrderViewState extends State<OrderView> {
  List<Order>? orders;

  TableRow _makeRow(Order order) {
    return TableRow();
  }

  @override
  Widget build(BuildContext context) {
    var header = const TableRow(children: []);

    return Column(
      children: [
        ScrollableTable(
            header: header,
            children: orders != null
                ? orders!
                    .where((element) => !element.isBuy)
                    .map(_makeRow)
                    .toList()
                : []),
        ScrollableTable(
            header: header,
            children: orders != null
                ? orders!
                    .where((element) => element.isBuy)
                    .map(_makeRow)
                    .toList()
                : []),
      ],
    );
  }
}
