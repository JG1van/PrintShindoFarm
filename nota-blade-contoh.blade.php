{{-- resources/views/penjualan/nota.blade.php --}}
<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="UTF-8">
<title>Nota Shindo Farm 77</title>
<style>
  @page { size: 58mm auto; margin: 0; }
  * { margin: 0; padding: 0; box-sizing: border-box; }
  html, body { width: 48mm; }
  body {
    font-family: 'Courier New', monospace;
    font-size: 10px;
    line-height: 1.4;
    color: #000;
    padding: 1mm 0;
  }
  .center { text-align: center; }
  .farm-name { font-size: 14px; font-weight: bold; letter-spacing: 1px; }
  .tagline { font-size: 9px; margin-bottom: 1mm; }
  .contact { font-size: 8px; line-height: 1.5; margin-bottom: 2mm; }
  .divider { border-top: 1px dashed #000; margin: 1.5mm 0; }
  .divider-solid { border-top: 1px solid #000; margin: 1.5mm 0; }
  table { width: 100%; border-collapse: collapse; }
  .info-table td { font-size: 9px; padding: 0.5mm 0; vertical-align: top; }
  .info-table .label { width: 40%; }
  .info-table .value { width: 60%; text-align: right; word-break: break-word; }
  .item-name { font-weight: bold; font-size: 10px; padding-top: 1mm; }
  .item-detail td { font-size: 9px; padding-bottom: 1mm; }
  .item-detail .qty { text-align: left; }
  .item-detail .price { text-align: right; }
  .total-table td { font-size: 9px; padding: 0.5mm 0; }
  .total-table .value { text-align: right; }
  .grand-total td { font-weight: bold; font-size: 12px; padding-top: 1mm; }
  .grand-total .value { text-align: right; }
  .footer { text-align: center; font-size: 8px; margin-top: 2mm; line-height: 1.5; }
  .footer-thanks { font-weight: bold; font-size: 10px; margin-bottom: 1mm; }
</style>
</head>
<body id="nota-content">

  <div class="center">
    <div class="farm-name">SHINDO FARM 77</div>
    <div class="tagline">Telur Ayam Kampung Segar</div>
    <div class="contact">
      Jl. Sonopakis Kidul, Ngestiharjo<br>
      Kasihan, Bantul, DIY<br>
      WA 0878-3921-0796
    </div>
  </div>

  <div class="divider-solid"></div>

  <table class="info-table">
    <tr><td class="label">Tanggal</td><td class="value">{{ \Carbon\Carbon::parse($penjualan->tanggal)->format('d/m/Y') }}</td></tr>
    <tr><td class="label">Pembeli</td><td class="value">{{ $penjualan->nama_pembeli }}</td></tr>
  </table>

  <div class="divider"></div>

  <table>
    <tr><td class="item-name" colspan="2">Telur Ayam Kampung</td></tr>
    <tr class="item-detail">
      <td class="qty">{{ $penjualan->jumlah_kg }} kg x {{ number_format($penjualan->harga_per_kg, 0, ',', '.') }}</td>
      <td class="price">{{ number_format($penjualan->subtotal, 0, ',', '.') }}</td>
    </tr>
    @if($penjualan->bonus > 0)
    <tr><td class="item-name" colspan="2">Bonus</td></tr>
    <tr class="item-detail">
      <td class="qty">{{ $penjualan->bonus }} butir</td>
      <td class="price">-</td>
    </tr>
    @endif
  </table>

  <div class="divider"></div>

  <table class="total-table">
    <tr><td class="label">Subtotal</td><td class="value">Rp {{ number_format($penjualan->subtotal, 0, ',', '.') }}</td></tr>
  </table>

  <div class="divider"></div>

  <table>
    <tr class="grand-total">
      <td class="label">TOTAL</td>
      <td class="value">Rp {{ number_format($penjualan->total, 0, ',', '.') }}</td>
    </tr>
  </table>

  <div class="divider"></div>

  <div class="footer">
    <div class="footer-thanks">Terima Kasih!</div>
    <div>Barang yang sudah dibeli<br>tidak dapat dikembalikan</div>
    <div style="margin-top:2mm;">shindo-farm-77.my.id</div>
  </div>

  <script>
    // Jembatan ke aplikasi Android ShindoFarmPrint (WebView)
    window.AndroidPrint = window.AndroidPrint || {
      printNota: function (content) {
        if (typeof AndroidPrint !== 'undefined' && AndroidPrint.cetakNota) {
          AndroidPrint.cetakNota(content);
        } else {
          // Fallback kalau dibuka di browser biasa (bukan dari app Android)
          window.print();
        }
      }
    };

    window.onload = function () {
      var notaText = document.getElementById('nota-content').innerText;
      AndroidPrint.printNota(notaText);
    };
  </script>

</body>
</html>
