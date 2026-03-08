package com.ecommerce.lab.config;

import java.util.List;
import java.util.Map;

public class ProductDetails {

        public static Map<String, List<String>> productNames = Map.of(
                "Electronics",
                List.of(
                        "Wireless Headphones",
                        "4K Ultra HD TV",
                        "Smartphone",
                        "Laptop Pro",
                        "Tablet 10-inch",
                        "Bluetooth Speaker",
                        "Smart Watch",
                        "Gaming Console",
                        "Digital Camera",
                        "Wireless Mouse",
                        "Mechanical Keyboard",
                        "External SSD"
                ),
                "Clothing",
                List.of(
                        "Men's Cotton T-Shirt",
                        "Women's Jeans",
                        "Hoodie Sweatshirt",
                        "Running Shorts",
                        "Leather Jacket",
                        "Summer Dress",
                        "Wool Sweater",
                        "Sports Bra",
                        "Yoga Pants",
                        "Denim Jacket",
                        "Polo Shirt",
                        "Winter Coat"
                ),
                "Home & Garden",
                List.of(
                        "Coffee Maker",
                        "Blender",
                        "Bed Sheets Set",
                        "Throw Pillows",
                        "Garden Tool Set",
                        "Indoor Plant",
                        "Desk Lamp",
                        "Bath Towels",
                        "Wall Clock",
                        "Kitchen Knife Set",
                        "Curtains",
                        "Vacuum Cleaner"
                ),
                "Books",
                List.of(
                        "The Great Novel",
                        "Science Fiction Trilogy",
                        "Cookbook",
                        "Self-Help Guide",
                        "History of Time",
                        "Children's Storybook",
                        "Poetry Collection",
                        "Biography",
                        "Travel Guide",
                        "Language Learning",
                        "Art Book",
                        "Mystery Novel"
                ),
                "Sports",
                List.of(
                        "Yoga Mat",
                        "Dumbbell Set 10kg",
                        "Basketball",
                        "Tennis Racket",
                        "Running Shoes",
                        "Water Bottle",
                        "Fitness Tracker",
                        "Jump Rope",
                        "Resistance Bands",
                        "Soccer Ball",
                        "Gym Gloves",
                        "Sports Bag"
                ),
                "Ramadan",
                List.of(
                        "Prayer Mat",
                        "Holy Quran",
                        "Ramadan Lantern",
                        "Misbaha Beads",
                        "Premium Dates",
                        "Incense Burner",
                        "Abaya",
                        "Islamic Wall Art",
                        "Prayer Beads (Tasbih)",
                        "Gift Hamper",
                        "Attar Oil",
                        "Traditional Kettle"
                )
        );

        // Descriptions by category
        public static Map<String, List<String>> descriptions = Map.of(
                "Electronics",
                List.of(
                        "High-quality device with latest technology",
                        "Premium materials and sleek design",
                        "Long battery life and fast performance",
                        "Water-resistant and durable construction"
                ),
                "Clothing",
                List.of(
                        "Made from 100% organic cotton",
                        "Comfortable fit for all-day wear",
                        "Machine washable and colorfast",
                        "Breathable fabric ideal for all seasons"
                ),
                "Home & Garden",
                List.of(
                        "Eco-friendly and sustainable materials",
                        "Adds style to any room",
                        "Easy to assemble and maintain",
                        "Weather-resistant for outdoor use"
                ),
                "Books",
                List.of(
                        "Bestselling author",
                        "Beautiful hardcover edition",
                        "Includes illustrations and diagrams",
                        "Perfect gift for book lovers"
                ),
                "Sports",
                List.of(
                        "Professional grade quality",
                        "Ergonomic design for comfort",
                        "Durable and long-lasting",
                        "Lightweight and portable"
                ),
                "Ramadan",
                List.of(
                        "Professional grade quality",
                        "Ergonomic design for comfort",
                        "Durable and long-lasting",
                        "Lightweight and portable"
                )
        );

}
