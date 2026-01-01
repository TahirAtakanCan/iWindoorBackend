package com.atablood.iWindoor_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "window_units")
public class WindowUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Örn: "Poz 1 - Mutfak"

    // Toplam Genişlik ve Yükseklik (mm)
    private Double width;
    private Double height;

    private Integer quantity = 1; // Kaç adet üretilecek?

    // Birim Fiyat (Tek bir pencerenin fiyatı)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // CRITICAL: Ağacın Kökü (Root Node)
    // Her pencere, bir ana çerçeve (Frame) ile başlar.
    // O çerçeve kendi içinde bölünür. İşte o kök düğüm burası.
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "root_node_id")
    private WindowNode rootNode;
}
