import pymysql
import sys
sys.stdout.reconfigure(encoding='utf-8')

conn = pymysql.connect(host='localhost', user='root', password='root', database='lobster_trade', charset='utf8mb4')
cur = conn.cursor()

cur.execute("SHOW TABLES")
tables = [t[0] for t in cur.fetchall()]
print("Tables:", tables)

for table in tables:
    cur.execute(f"SHOW CREATE TABLE {table}")
    create_stmt = cur.fetchone()[1]
    print(f"\n\n-- ============================================================")
    print(f"-- Table: {table}")
    print(f"-- ============================================================")
    print(create_stmt)

conn.close()
