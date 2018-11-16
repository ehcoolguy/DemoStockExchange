import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { LiveStockPriceService } from '../../live-stock-price.service';
import { LivedataPanelComponent } from '../../exchange/livedata-panel/livedata-panel.component';
import { filter } from 'rxjs/operators';
import { MsgRateCalculatorService } from 'src/app/msg-rate-calculator.service';

@Component({
  selector: 'app-perf-summary',
  templateUrl: './perf-summary.component.html',
  styleUrls: ['./perf-summary.component.css']
})
export class PerfSummaryComponent implements OnInit {
  public TXT_PERF_SUMMARY_TITLE = '系統效能';
  public TXT_PERF_MATCHING_SPEED = '交易速度';
  public TXT_PERF_MATCHING_AMOUNT = '交易筆數';
  public TXT_PERF_MARKET_DATA_SPEED = '行情速度';
  public TXT_PERF_MARKET_DATA_BW = '使用頻寬';
  public TXT_UNIT_MSG_RATE = '筆/秒';
  public TXT_UNIT_MSG_AMOUNT = '筆';
  public TXT_UNIT_BW = 'bytes';

  public nMatchingSpeed: number;
  public nMatcingAmount: number;
  public nMarketDataSpeed: number;
  public strMarketDataSpeed: string;
  public strMarketDataBw: string;

  private marketStatusTopic = 'TW/TWSE/STATUS';

  constructor(
    private liveStockPriceSvc: LiveStockPriceService,
    private changeDetector: ChangeDetectorRef,
    private msgRateCalcSvc: MsgRateCalculatorService) {
    this.nMatchingSpeed = 0.0;
    this.nMatcingAmount = 0;
    this.nMarketDataSpeed = 0.0;
    this.strMarketDataSpeed = '0.0';
    this.strMarketDataBw = '0.0';
  }

  ngOnInit() {
    this.liveStockPriceSvc.svcStatus$.subscribe({
      next: (value) => {
        if (value === true) {
          // with filter
          this.liveStockPriceSvc.mySolclient.onMessage
            .pipe(filter((x: any) => x.msg.getDestination().getName().startsWith(this.marketStatusTopic)))
            .subscribe(ev => {
              this.updateExchangeStatus(ev.msg.getBinaryAttachment());
            });
          this.msgRateCalcSvc.startCalc();
          this.msgRateCalcSvc.nMdMsgRate.subscribe({
            next: (mr) => {
              this.nMarketDataSpeed = mr;
              // console.log('MsgRate is ' + mr);
            }
          });
          this.msgRateCalcSvc.strMdTotalBwUsage.subscribe({
            next: (mtbw) => {
              this.strMarketDataBw = mtbw;
              // console.log('Total BW is ' + mtbw);
            }
          });
        } else {
          this.msgRateCalcSvc.stopCalc();
          // this.msgRateCalcSvc.strMdTotalBwUsage.unsubscribe();
          // this.liveStockPriceSvc.mySolclient.onMessage.unsubscribe();
        }
      }
    });
  }

  private updateExchangeStatus(strMdRawData: string) {
    const arMdRawData = strMdRawData.split('|');

    if (arMdRawData[0].indexOf('EXM') > 0) {
      // Display the current Match Engine Rate
      if (arMdRawData[0].indexOf('EXM000101') > 0) {
        this.nMatchingSpeed = Number(arMdRawData[2]);
      } else if (arMdRawData[0].indexOf('EXM000110') > 0) {
        this.nMatcingAmount = Number(arMdRawData[2]);
      }
    }
    this.changeDetector.detectChanges();
  }
}
