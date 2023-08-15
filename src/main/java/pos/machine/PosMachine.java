package pos.machine;

import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class PosMachine {
    public String printReceipt(List<String> barcodes) {
        List<ReceiptItem> receiptItems = decodeToItems(barcodes);
        calculateItemsCost(receiptItems);
        int totalCost = calculateTotalCost(receiptItems);
        return renderReceipt(receiptItems,totalCost);
    }

    public static List<ReceiptItem> decodeToItems(List<String> barcodes){
        List<Item> items = ItemsLoader.loadAllItems();

        Map<String, Item> itemMap = items.stream().collect(Collectors.toMap(Item::getBarcode, item -> item));

        Map<String, Long> barcodeCountMap = barcodes.stream().collect(Collectors.groupingBy(barcode -> barcode, Collectors.counting()));

        return barcodeCountMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
            String barcode = entry.getKey();
            long quantity = entry.getValue();
            Item item = itemMap.get(barcode);
            if(item != null){
                int subtotal = (int) (item.getPrice() * quantity);
                return new ReceiptItem(item.getName(),(int) quantity,item.getPrice(),subtotal);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void calculateItemsCost(List<ReceiptItem> receiptItems){
        receiptItems.forEach(ReceiptItem::calculateSubTotal);
    }

    public int calculateTotalCost(List<ReceiptItem> receiptItems){
        return receiptItems.stream().mapToInt(ReceiptItem::getSubTotal).sum();
    }

    public String generateItemsReceipt(ReceiptItem receiptItem){
        return String.format("Name: %s, Quantity: %d, Unit price: %d (yuan), Subtotal: %d (yuan)",
                receiptItem.getName(), receiptItem.getQuantity(), receiptItem.getUnitPrice(), receiptItem.getSubTotal());
    }

    public String generateReceipt(List<ReceiptItem> receiptItems, int totalCost){
        StringBuilder receipt = new StringBuilder();
        receipt.append("***<store earning no money>Receipt***\n");
        receiptItems.forEach(item -> receipt.append(generateItemsReceipt(item)).append("\n"));
        receipt.append("----------------------\n");
        receipt.append("Total: ").append(totalCost).append(" (yuan)\n");
        receipt.append("**********************");

        return receipt.toString();
    }

    public String renderReceipt(List<ReceiptItem> receiptItems, int totalCost){
        return generateReceipt(receiptItems,totalCost);
    }

}
