import pymysql

conn = pymysql.connect(host='localhost', user='root', password='root', database='lobster_trade', charset='utf8mb4')
cur = conn.cursor()

cur.execute("SHOW TABLES")
tables = [t[0] for t in cur.fetchall()]

with open('C:\\Users\\Administrator\\.openclaw\\workspace\\projects\\龙虾道具交易平台\\code\\lobster-trade-server\\db\\all_tables_utf8.sql', 'w', encoding='utf-8') as f:
    f.write("-- Exported from live database at localhost/lobster_trade\n")
    f.write("-- Tables: " + str(tables) + "\n\n")
    
    for table in tables:
        cur.execute(f"SHOW CREATE TABLE {table}")
        create_stmt = cur.fetchone()[1]
        f.write(f"\n-- ============================================================\n")
        f.write(f"-- Table: {table}\n")
        f.write(f"-- ============================================================\n")
        f.write(create_stmt)
        f.write(";\n\n")

conn.close()
print("Done. Tables exported:", len(tables))
