package ru.barabo.babloz.db.entity

import ru.barabo.db.annotation.*
import ru.barabo.db.converter.BooleanConverter
import ru.barabo.db.converter.EnumConverter
import java.math.BigDecimal
import java.time.LocalDate

private const val PERCENT_SIMPLE = """(
with CUR_RANGE as (

select case when strftime('%d', DAY_SIMPLE_PERCENT) >= strftime('%d', DATE('NOW', 'LOCALTIME') )
  then date(strftime('%Y', DATE('NOW', 'LOCALTIME') ) || '-' ||   strftime('%m', date(DATE('NOW', 'LOCALTIME'), '-1 month') ) || '-' || strftime('%d', DAY_SIMPLE_PERCENT) )
  else date(strftime('%Y', DATE('NOW', 'LOCALTIME')) || '-' ||   strftime('%m', DATE('NOW', 'LOCALTIME')) || '-' || strftime('%d', DAY_SIMPLE_PERCENT) )
  end start_perc,
  
  case when strftime('%d', DAY_SIMPLE_PERCENT) > strftime('%d', DATE('NOW', 'LOCALTIME') )
  then date(strftime('%Y', DATE('NOW', 'LOCALTIME') ) || '-' ||   strftime('%m', DATE('NOW', 'LOCALTIME')) || '-' || strftime('%d', DAY_SIMPLE_PERCENT) )
  else date(strftime('%Y', DATE('NOW', 'LOCALTIME') ) || '-' ||   strftime('%m', date(DATE('NOW', 'LOCALTIME'), '+1 month') ) || '-' || strftime('%d', DAY_SIMPLE_PERCENT) )
  end end_perc,
  
  id account,
  
  simple_percent
  
from account
where simple_percent is not null
  and DAY_SIMPLE_PERCENT is not null
),

pay_account as (

select date(pp.created) created, cr.account account, cr.start_perc, cr.end_perc, cr.simple_percent
     from PAY pp
     join CUR_RANGE cr on  cr.account in (pp.ACCOUNT, pp.ACCOUNT_TO)
     
     where COALESCE(pp.SYNC, 0) != 2  
     and pp.created <= cr.end_perc
     and pp.created > cr.start_perc
     
     group by date(pp.created), cr.account, cr.start_perc, cr.end_perc, cr.simple_percent
),

account_end as (
 select min(cr.end_perc, DATE('NOW', 'LOCALTIME') ) created, cr.account account,  cr.start_perc, cr.end_perc, cr.simple_percent

  from CUR_RANGE cr
),

PAY_ALL as (
select pa.created, pa.account, pa.start_perc, pa.end_perc, pa.simple_percent
from pay_account pa 
left join account_end on account_end.account = pa.account and account_end.created = pa.created
group by pa.created, pa.account, pa.start_perc, pa.end_perc, pa.simple_percent

union

select account_end.created created, account_end.account,  account_end.start_perc, account_end.end_perc, account_end.simple_percent
from account_end
left join pay_account pa on account_end.account = pa.account and account_end.created = pa.created
group by coalesce(pa.created, account_end.created), account_end.account, account_end.start_perc, account_end.end_perc, pa.simple_percent
),

ALL_PERC as (
select
  COALESCE( round(
  COALESCE((select sum(case when p.account = pp.ACCOUNT then pp.AMOUNT else COALESCE(pp.amount_to, -1*pp.AMOUNT) end)
   from PAY pp
   where COALESCE(pp.SYNC, 0) != 2  
     and pp.created < date(p.created)
     and p.account in (pp.ACCOUNT, pp.ACCOUNT_TO)
  ), 0) *
  
  p.simple_percent *
  
  (  
  JULIANDAY(p.created) - 
  
  JULIANDAY( COALESCE( (select max(date(pp.created)) 
  from PAY pp 
  where COALESCE(pp.SYNC, 0) != 2
    and p.account in (pp.ACCOUNT, pp.ACCOUNT_TO)
    and date(pp.created) >= date(p.start_perc)
    and date(pp.created) < date(p.created)
  ), date(p.start_perc)) ) 
  ) / 36500, 2), 0) PERC,
  
 p.account, 
 
 p.created

from PAY_ALL p
group by p.account, p.created
)

select round( sum(ALL_PERC.PERC), 2 )
from ALL_PERC
where ALL_PERC.account = A.id
) PERCENT_SIMPLE """


private const val PERCENT_ADD = """(
with pay_account as (

select date(pp.created) created, aa.ID account, aa.start_add, aa.end_add, aa.add_percent
     from PAY pp
     join account aa on  aa.ID in (pp.ACCOUNT, pp.ACCOUNT_TO)
     where COALESCE(pp.SYNC, 0) != 2  
     and pp.created <= aa.end_add
     and pp.created > aa.start_add
     group by date(pp.created), aa.ID, aa.start_add, aa.end_add, aa.add_percent
),

account_end as (
 select min(date(a.end_add), DATE('NOW', 'LOCALTIME') ) created, a.id account, a.start_add, a.end_add, a.add_percent
  from account a
  where date(a.start_add) <= DATE('NOW', 'LOCALTIME')
  group by  a.id, a.start_add, a.end_add, a.add_percent
),

PAY_ALL as (
select pa.created, pa.account, pa.start_add, pa.end_add, pa.add_percent
from pay_account pa 
left join account_end on account_end.account = pa.account and account_end.created = pa.created
group by pa.created, pa.account,  pa.start_add, pa.end_add, pa.add_percent

union

select account_end.created created, account_end.account,  account_end.start_add, account_end.end_add, account_end.add_percent
from account_end
left join pay_account pa on account_end.account = pa.account and account_end.created = pa.created
group by coalesce(pa.created, account_end.created), account_end.account, account_end.start_add, account_end.end_add, pa.add_percent
),

ALL_PERC as (
select
  COALESCE( round(
  COALESCE((select sum(case when p.account = pp.ACCOUNT then pp.AMOUNT else COALESCE(pp.amount_to, -1*pp.AMOUNT) end)
   from PAY pp
   where COALESCE(pp.SYNC, 0) != 2  
     and pp.created < date(p.created)
     and p.account in (pp.ACCOUNT, pp.ACCOUNT_TO)
  ), 0) *
  
  p.add_percent *
  
  (  
  JULIANDAY(p.created) - 
  
  JULIANDAY( COALESCE( (select max(date(pp.created)) 
  from PAY pp 
  where COALESCE(pp.SYNC, 0) != 2
    and p.account in (pp.ACCOUNT, pp.ACCOUNT_TO)
    and date(pp.created) >= date(p.start_add)
    and date(pp.created) < date(p.created)
  ), date(p.start_add)) ) 
  ) / 36500, 2), 0) PERC,
  
 p.account, 
 
 p.created

from PAY_ALL p
group by p.account, p.created
)

select round( sum(ALL_PERC.PERC), 2 )
from ALL_PERC
where ALL_PERC.account = A.id
)  PERCENT_ADD """


@TableName("ACCOUNT")
@SelectQuery("""
    select a.ID, a.NAME, a.DESCRIPTION, a.TYPE, a.CLOSED, a.CURRENCY, c.name CUR_NAME, c.EXT CUR_EXT, a.IS_USE_DEBT, a.SYNC,
    
    a.SIMPLE_PERCENT, a.DAY_SIMPLE_PERCENT, a.ADD_PERCENT, a.START_ADD, a.END_ADD,

    (select COALESCE(sum(case when a.ID = p.ACCOUNT then p.AMOUNT else COALESCE(p.amount_to, -1*p.AMOUNT) end), 0)
      from PAY p where a.ID in (p.ACCOUNT, p.ACCOUNT_TO) and COALESCE(p.SYNC, 0) != 2) REST,
      
    $PERCENT_ADD,
      
    $PERCENT_SIMPLE  

    from ACCOUNT a, CURRENCY c
    where a.CURRENCY = c.ID
      and (CLOSED IS NULL OR CLOSED > CURRENT_DATE)
      and COALESCE(a.SYNC, 0) != 2
     order by a.TYPE""")
data class Account (
    @ColumnName("ID")
    @SequenceName("SELECT COALESCE(MIN(ID), 0) - 1  from ACCOUNT")
    @ColumnType(java.sql.Types.INTEGER)
    var id: Int? = null,

    @ColumnName("NAME")
    @ColumnType(java.sql.Types.VARCHAR)
    var name: String? = null,

    @ColumnName("DESCRIPTION")
    @ColumnType(java.sql.Types.VARCHAR)
    var description: String? = null,

    @ColumnName("TYPE")
    @ColumnType(java.sql.Types.INTEGER)
    @Converter(EnumConverter::class)
    var type: AccountType? = null,

    @ColumnName("CLOSED")
    @ColumnType(java.sql.Types.DATE)
    var closed: LocalDate? = null,

    @ColumnName("CURRENCY")
    @ColumnType(java.sql.Types.INTEGER)
    @ManyToOne("CUR_")
    var currency: Currency? = null,

    @ColumnName("SIMPLE_PERCENT")
    @ColumnType(java.sql.Types.NUMERIC)
    var simplePercent: BigDecimal? = null,

    @ColumnName("DAY_SIMPLE_PERCENT")
    @ColumnType(java.sql.Types.DATE)
    var daySimplePercent: LocalDate? = null,

    @ColumnName("ADD_PERCENT")
    @ColumnType(java.sql.Types.NUMERIC)
    var addPercent: BigDecimal? = null,

    @ColumnName("START_ADD")
    @ColumnType(java.sql.Types.DATE)
    var startAdd: LocalDate? = null,

    @ColumnName("END_ADD")
    @ColumnType(java.sql.Types.DATE)
    var endAdd: LocalDate? = null,

    @ColumnName("REST")
    @ColumnType(java.sql.Types.NUMERIC)
    @ReadOnly
    var rest :BigDecimal? = null,

    @ColumnName("PERCENT_ADD")
    @ColumnType(java.sql.Types.NUMERIC)
    @CalcColumnQuery("select $PERCENT_ADD from account A where A.id = ?")
    @ReadOnly
    var percentAdd :BigDecimal? = null,

    @ColumnName("PERCENT_SIMPLE")
    @ColumnType(java.sql.Types.NUMERIC)
    @CalcColumnQuery("select $PERCENT_SIMPLE from account A where A.id = ?")
    @ReadOnly
    var percentSimple :BigDecimal? = null,

    @ColumnName("IS_USE_DEBT")
    @ColumnType(java.sql.Types.INTEGER)
    @Converter(BooleanConverter::class)
    var isUseDebt: Boolean = false,

    var isSelected : Int? = null,

        @ColumnName("SYNC")
    @ColumnType(java.sql.Types.INTEGER)
    @Transient
    var sync :Int? = null
    ) {

    override fun toString(): String = name?:""

    override fun equals(other: Any?): Boolean {

        if(this === other) return true

        if(other is Account?) {
            return (this.id == other?.id && this.name == other?.name)
        }
        return false
    }

    override fun hashCode(): Int = (id ?: (0 * 31 + (name?.hashCode() ?: 0))) * 31
}



