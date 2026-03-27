select
    o.id,
    o.buyer_username,
    o.status,
    o.total_minor,
    o.create_at
from ex_order o
where o.buyer_username = /* buyerUsername */'buyer-1'
order by o.create_at desc
